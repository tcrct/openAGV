package com.robot.adapter;

import com.google.inject.assistedinject.Assisted;
import com.robot.config.AgvConfiguration;
import com.robot.config.LoadAction;
import com.robot.config.LoadState;
import com.robot.contrib.netty.comm.IChannelManager;
import com.robot.mvc.core.exceptions.AgvException;
import com.robot.mvc.core.interfaces.IRequest;
import com.robot.mvc.core.interfaces.IResponse;
import com.robot.mvc.core.telegram.ITelegram;
import com.robot.mvc.core.telegram.ITelegramSender;
import com.robot.mvc.utils.AgvKit;
import org.opentcs.components.kernel.services.TCSObjectService;
import org.opentcs.contrib.tcp.netty.ConnectionEventListener;
import org.opentcs.customizations.kernel.KernelExecutor;
import org.opentcs.data.model.Vehicle;
import org.opentcs.data.order.DriveOrder;
import org.opentcs.drivers.vehicle.BasicVehicleCommAdapter;
import org.opentcs.drivers.vehicle.MovementCommand;
import org.opentcs.drivers.vehicle.VehicleCommAdapterPanel;
import org.opentcs.util.ExplainedBoolean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;

import static java.util.Objects.requireNonNull;

/**
 * Agv通讯适配器
 * 一台车辆对应一个适配器
 *
 * @author Laotang
 */
public class RobotCommAdapter
        extends BasicVehicleCommAdapter
        implements ConnectionEventListener<ITelegram>,ITelegramSender {

    private static final Logger LOG = LoggerFactory.getLogger(RobotCommAdapter.class);

    /**大杀器*/
    private TCSObjectService tcsObjectService;
    /**执行器*/
    private ExecutorService kernelExecutor;
    /**适配器组件工厂*/
    private AdapterComponentsFactory componentsFactory;
    /**配置文件类*/
    private AgvConfiguration configuration;
    /**车辆*/
    private Vehicle vehicle;
    /**移动命令监听器*/
    private MoveCommandListener moveCommandListener;
    /**移动请求任务定时器，一车辆实例一次*/
    private MoveRequesterTask moveRequesterTask;
    /**移动命令队列*/
    private Queue<MovementCommand> movementCommandQueue = new LinkedBlockingQueue<>();
    /**车辆网络连接管理器*/
    private IChannelManager<IRequest, IResponse> vehicleChannelManager;
    /**运行方式，以服务器方式运行还是客户端方式链接车辆*/
    private String runType;

    @Inject
    public RobotCommAdapter(AdapterComponentsFactory componentsFactory,
                            TCSObjectService tcsObjectService,
                            AgvConfiguration configuration,
                            @Assisted Vehicle vehicle,
                            @KernelExecutor ExecutorService kernelExecutor) {
        super(new RobotProcessModel(vehicle),
                configuration.commandQueueCapacity(),
                configuration.sentQueueCapacity(),
                configuration.rechargeOperation());

        this.tcsObjectService = requireNonNull(tcsObjectService, "tcsObjectService");
        this.vehicle = requireNonNull(vehicle, "vehicle");
        this.configuration = requireNonNull(configuration, "configuration");
        this.componentsFactory = requireNonNull(componentsFactory, "componentsFactory");
        this.kernelExecutor = requireNonNull(kernelExecutor, "kernelExecutor");
    }

    /**
     * 初始化适配器
     */
    @Override
    public void initialize() {
        //每一个车辆一个定时监听器
        moveCommandListener = new MoveCommandListener(this);
        moveRequesterTask = new MoveRequesterTask(moveCommandListener);
        // 初始化车辆渠道管理器
        if (null == vehicleChannelManager) {
            vehicleChannelManager = VehicleChannelManager.getChannelManager(this);
            if (!vehicleChannelManager.isInitialized()) {
                vehicleChannelManager.initialize();
            }
        }
        super.initialize();
        LOG.info("车辆[{}]完成Robot适配器初始化完成", getName());
    }

    @Override
    public synchronized void enable() {
        if (isEnabled()) {
            LOG.info("车辆[{}]已开启通讯管理器，请勿重复开启", getName());
            return;
        }

        // 如果是以客户端的方式来启动，则在开启车辆时完成链接，RXTX方式除外
        if ("client".equalsIgnoreCase(runType)) {
            String host = AgvKit.getHost(getName());
            int port = AgvKit.getPort(getName());

        }
        super.enable();
    }

    public synchronized void trigger() {

    }

    /**
     * 创建适配器面板
     * @return
     */
    @Override
    protected List<VehicleCommAdapterPanel> createAdapterPanels() {
        return null;
    }

    /**连接车辆*/
    @Override
    protected void connectVehicle() {
        if (null == vehicleChannelManager) {
            LOG.warn("车辆[{}]通讯渠道管理器不存在", getName());
            return;
        }
        // 根据车辆设置的host与port，连接车辆
        String host = AgvKit.getHost(getName());
        int port = AgvKit.getPort(getName());
        try {
            vehicleChannelManager.connect(host, port);
            LOG.info("连接车辆[{}]成功: [{}]", getName(), (host + ":" + port));
        } catch (AgvException e) {
            throw e;
        }
    }

    /**断开车辆连接*/
    @Override
    protected void disconnectVehicle() {
        if (null == vehicleChannelManager) {
            LOG.warn("车辆[{}]通讯渠道管理器不存在.", getName());
            return;
        }

        vehicleChannelManager.disconnect();
    }

    /**判断车辆是否已经连接*/
    @Override
    protected boolean isVehicleConnected() {
        return null != vehicleChannelManager && vehicleChannelManager.isConnected();
    }

    /**是否执行进程操作，车辆移动命令发送前检查*/
    @Nonnull
    @Override
    public ExplainedBoolean canProcess(@Nonnull List<String> operations) {
        requireNonNull(operations, "operations");
        boolean canProcess = true;
        String reason = "";

        if(!isEnabled()) {
            canProcess = false;
            reason= "通讯适配器没有开启";
        }

        if(canProcess && !isVehicleConnected()) {
            canProcess = false;
            reason = "车辆可能没有连接";
        }

        if(canProcess &&
                LoadState.UNKNOWN.name().equalsIgnoreCase(getProcessModel().getVehicleState().name())) {
            canProcess = false;
            reason = "车辆负载状态未知";
        }

        boolean loaded = LoadState.FULL.name().equalsIgnoreCase(getProcessModel().getVehicleState().name());
        final Iterator<String> iterator = operations.iterator();
        while (canProcess && iterator.hasNext()) {
            final String nextOp = iterator.next();
            if(loaded) {
                if (LoadAction.LOAD.equalsIgnoreCase(nextOp)) {
                    canProcess = false;
                    reason = "不能重复装载";
                } else if (LoadAction.UNLOAD.equalsIgnoreCase(nextOp)) {
                    loaded = false;
                } else if (DriveOrder.Destination.OP_PARK.equalsIgnoreCase(nextOp)) {
                    canProcess = false;
                    reason = "车辆在装载状态下不应该停车";
                } else if (LoadAction.CHARGE.equalsIgnoreCase(nextOp)) {
                    canProcess = false;
                    reason = "车辆在装载状态下不应该充电";
                }
            } else if (LoadAction.LOAD.equalsIgnoreCase(nextOp)){
                loaded = true;
            } else if(LoadAction.UNLOAD.equalsIgnoreCase(nextOp)){
                canProcess = false;
                reason = "未加载时无法卸载";
            }
        }
        return new ExplainedBoolean(canProcess, reason);
    }

    /**
     * 发送移动命令
     * 当有多个车辆需要进行交通管制时，
     * 以下方法会自动对应的MovementCommand，告诉可以使用的MC对象
     * 利用这个回调发送移动命令，再次处理后，将协议发送到车辆
     *
     * @param cmd The command to be sent.
     * @throws IllegalArgumentException
     */
    @Override
    public void sendCommand(MovementCommand cmd) throws IllegalArgumentException {
        cmd = requireNonNull(cmd, "MovementCommand is null");
        // 添加到队列
        movementCommandQueue.add(cmd);
        // 监听器引用队列，处理后发送协议
        moveCommandListener.quoteCommand(movementCommandQueue);
    }

    /**进程消息*/
    @Override
    public void processMessage(@Nullable Object message) {
        LOG.info("processMessage: {}", message);
    }

    /**取车辆进程模型*/
    @Override
    public final RobotProcessModel getProcessModel() {
        return (RobotProcessModel) super.getProcessModel();
    }

    /**
     * 取移动命令队列
     * @return
     */
    public Queue<MovementCommand> getMovementCommandQueue() {
        return movementCommandQueue;
    }


    //*********************************ConnectionEventListener*************************************/

    /**
     * 接收到报文信息
     * @param telegram 电报对象
     */
    @Override
    public void onIncomingTelegram(ITelegram telegram) {
        requireNonNull(telegram, "response");

        // 车辆状态设置为不空闲
        getProcessModel().setVehicleIdle(false);
        // 如果是交通管制，此时的队列不为空，则需要将第一位的元素移除
        if (!movementCommandQueue.isEmpty()) {
            movementCommandQueue.remove();
        }
    }

    /**链接车辆*/
    @Override
    public void onConnect() {
        if (!isEnabled()) {
            return;
        }
        getProcessModel().setCommAdapterConnected(true);
        LOG.debug("车辆[{}]连接成功", getName());
    }

    /***/
    @Override
    public void onFailedConnectionAttempt() {
        if (!isEnabled()) {
            return;
        }
        getProcessModel().setCommAdapterConnected(false);
        if (isEnabled() && getProcessModel().isReconnectingOnConnectionLoss()) {
            vehicleChannelManager.scheduleConnect(AgvKit.getHost(getName()), AgvKit.getPort(getName()), getProcessModel().getReconnectDelay());
        }
    }

    /**断开链接*/
    @Override
    public void onDisconnect() {
        LOG.debug("车辆[{}]断开连接成功", getName());
        getProcessModel().setCommAdapterConnected(false);
        getProcessModel().setVehicleIdle(true);
        getProcessModel().setVehicleState(Vehicle.State.UNKNOWN);
        if (isEnabled() && getProcessModel().isReconnectingOnConnectionLoss()) {
            vehicleChannelManager.scheduleConnect(AgvKit.getHost(getName()), AgvKit.getPort(getName()), getProcessModel().getReconnectDelay());
        }
    }

    /**空闲*/
    @Override
    public void onIdle() {
        LOG.debug("车辆[{}]空闲", getName());
        getProcessModel().setVehicleIdle(true);
        // 如果支持重连则的车辆空间时断开连接
        if (isEnabled() && getProcessModel().isDisconnectingOnVehicleIdle()) {
            LOG.debug("车辆[{}]开启了空闲时断开连接", getName());
            disconnectVehicle();
        }
    }

    //*********************************ITelegramSender*************************************/
    /**
     * 发送报文
     * @param telegram 电报对象
     */
    @Override
    public void sendTelegram(ITelegram telegram) {

    }


}
