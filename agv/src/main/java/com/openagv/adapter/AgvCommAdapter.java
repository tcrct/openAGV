package com.openagv.adapter;

import com.google.inject.assistedinject.Assisted;
import com.openagv.AgvContext;
import com.openagv.config.AgvConfiguration;
import com.openagv.mvc.core.exceptions.AgvException;
import com.openagv.mvc.core.telegram.MoveRequest;
import com.openagv.mvc.core.telegram.ITelegram;
import com.openagv.mvc.core.telegram.ITelegramSender;
import com.openagv.mvc.main.DispatchFactory;
import org.opentcs.components.kernel.services.TCSObjectService;
import org.opentcs.contrib.tcp.netty.ConnectionEventListener;
import org.opentcs.customizations.kernel.KernelExecutor;
import org.opentcs.data.model.Vehicle;
import org.opentcs.drivers.vehicle.BasicVehicleCommAdapter;
import org.opentcs.drivers.vehicle.MovementCommand;
import org.opentcs.drivers.vehicle.VehicleCommAdapterPanel;
import org.opentcs.util.ExplainedBoolean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;

import static java.util.Objects.requireNonNull;

/**
 * Agv通讯适配器
 * 一台车辆对应一个适配器
 *
 * @blame Laotang
 */
public class AgvCommAdapter
        extends BasicVehicleCommAdapter
        implements ConnectionEventListener<ITelegram>,ITelegramSender {

    private static final Logger LOG = LoggerFactory.getLogger(AgvCommAdapter.class);

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

    @Inject
    public AgvCommAdapter(AdapterComponentsFactory componentsFactory,
                                        TCSObjectService tcsObjectService,
                                        AgvConfiguration configuration,
                                        @Assisted Vehicle vehicle,
                                        @KernelExecutor ExecutorService kernelExecutor) {
        super(new VehicleModel(vehicle),
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
        super.initialize();
        moveCommandListener = new MoveCommandListener(AgvContext.getAdapter(getName()));
        moveRequesterTask = new MoveRequesterTask(moveCommandListener);
        LOG.info("车辆[{}]完成Robot适配器初始化完成", getName());
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

    }

    /**断开车辆连接*/
    @Override
    protected void disconnectVehicle() {

    }

    /**判断车辆是否已经连接*/
    @Override
    protected boolean isVehicleConnected() {
        return false;
    }

    /**是否执行进程操作，车辆移动命令发送前检查*/
    @Nonnull
    @Override
    public ExplainedBoolean canProcess(@Nonnull List<String> operations) {
        return null;
    }

    /**进程消息*/
    @Override
    public void processMessage(@Nullable Object message) {

    }

    /**
     * 发送报文
     * @param telegram 电报对象
     */
    @Override
    public void sendTelegram(ITelegram telegram) {

    }

    /**
     * 接收到报文信息
     * @param iTelegram 电报对象
     */
    @Override
    public void onIncomingTelegram(ITelegram iTelegram) {

    }

    @Override
    public void onConnect() {

    }

    @Override
    public void onFailedConnectionAttempt() {

    }

    @Override
    public void onDisconnect() {

    }

    @Override
    public void onIdle() {

    }

    @Override
    public final AgvProcessModel getProcessModel() {
        return (AgvProcessModel) super.getProcessModel();
    }
}
