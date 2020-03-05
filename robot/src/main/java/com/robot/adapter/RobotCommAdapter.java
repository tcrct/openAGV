package com.robot.adapter;

import cn.hutool.core.thread.ThreadUtil;
import com.google.inject.assistedinject.Assisted;
import com.robot.RobotContext;
import com.robot.adapter.enumes.LoadAction;
import com.robot.adapter.enumes.LoadState;
import com.robot.adapter.exchange.AdapterComponentsFactory;
import com.robot.adapter.model.*;
import com.robot.adapter.task.MoveCommandListener;
import com.robot.adapter.task.MoveRequesterTask;
import com.robot.commands.SetVehiclePausedCommand;
import com.robot.config.RobotConfiguration;
import com.robot.contrib.netty.ConnectionEventListener;
import com.robot.contrib.netty.comm.NetChannelType;
import com.robot.mvc.core.exceptions.RobotException;
import com.robot.mvc.core.interfaces.IAction;
import com.robot.mvc.core.interfaces.IResponse;
import com.robot.mvc.core.telegram.ITelegramSender;
import com.robot.utils.RobotUtil;
import com.robot.utils.ServerContribKit;
import com.robot.utils.ToolsKit;
import org.opentcs.components.kernel.services.TCSObjectService;
import org.opentcs.customizations.kernel.KernelExecutor;
import org.opentcs.data.model.Vehicle;
import org.opentcs.data.order.DriveOrder;
import org.opentcs.data.order.TransportOrder;
import org.opentcs.drivers.vehicle.BasicVehicleCommAdapter;
import org.opentcs.drivers.vehicle.MovementCommand;
import org.opentcs.drivers.vehicle.VehicleCommAdapterPanel;
import org.opentcs.drivers.vehicle.VehicleControllerPool;
import org.opentcs.drivers.vehicle.messages.SetSpeedMultiplier;
import org.opentcs.kernel.services.StandardDispatcherService;
import org.opentcs.kernel.services.StandardTransportOrderService;
import org.opentcs.kernel.services.StandardVehicleService;
import org.opentcs.strategies.basic.routing.DefaultRouter;
import org.opentcs.util.ExplainedBoolean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;
import java.util.*;
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
        implements ConnectionEventListener<RobotStateModel>, ITelegramSender {

    private static final Logger LOG = LoggerFactory.getLogger(RobotCommAdapter.class);

    /**
     * 大杀器
     */
    private TCSObjectService tcsObjectService;
    /**
     * 执行器
     */
    private ExecutorService kernelExecutor;
    /**
     * 适配器组件工厂
     */
    private AdapterComponentsFactory componentsFactory;
    /**
     * 配置文件类
     */
    private RobotConfiguration configuration;
    /**
     * 车辆
     */
    private Vehicle vehicle;
    /**
     * 移动命令定时发送监听器
     */
    private MoveCommandListener moveCommandListener;
    /**
     * 移动请求任务定时器，一车辆实例一次
     */
    private MoveRequesterTask moveRequesterTask;
    /**
     * 临时移动命令队列
     */
    private Queue<MovementCommand> tempCommandQueue;
    /**
     * 移动命令队列
     */
    private Queue<MovementCommand> movementCommandQueue;
    /**
     *  网络工具
     */
    private ServerContribKit contribKit;
    /**
     * 正在执行的工站动作名称集合，工站动作名称必须唯一
     * 如果Set里存在该动作名称，则代表动作正在执行，执行完成后，需要remove该动作名称
     */
    private static final Set<String> executeLocationActionNameSet = new HashSet<>();

    private StandardVehicleService vehicleService;
    private StandardTransportOrderService transportOrderService;
    private StandardDispatcherService dispatcherService;
    private DefaultRouter router;
    private VehicleControllerPool vehicleControllerPool;

    @Inject
    public RobotCommAdapter(AdapterComponentsFactory componentsFactory,
                            TCSObjectService tcsObjectService,
                            RobotConfiguration configuration,
                            StandardVehicleService vehicleService,
                            StandardTransportOrderService transportOrderService,
                            StandardDispatcherService dispatcherService,
                            DefaultRouter router,
                            VehicleControllerPool vehicleControllerPool,
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

        this.vehicleService = vehicleService;
        this.transportOrderService = transportOrderService;
        this.dispatcherService = dispatcherService;
        this.router = router;
        this.vehicleControllerPool = vehicleControllerPool;

        /**移动命令队列*/
        this.tempCommandQueue = new LinkedBlockingQueue<>();
        this.movementCommandQueue = new LinkedBlockingQueue<>();
        RobotContext.setTCSObjectService(tcsObjectService);
    }

    public StandardVehicleService getVehicleService() {
        return vehicleService;
    }

    public StandardTransportOrderService getTransportOrderService() {
        return transportOrderService;
    }

    public DefaultRouter getRouter() {
        return router;
    }

    public VehicleControllerPool getVehicleControllerPool() {
        return vehicleControllerPool;
    }

    public StandardDispatcherService getDispatcherService() {
        return dispatcherService;
    }

    /**
     * 初始化适配器
     */
    @Override
    public void initialize() {
        if (isInitialized()) {
            LOG.info("车辆[{}]已初始化通讯管理器，请勿重复初始化", getName());
            return;
        }
        super.initialize();
        // 初始化车辆渠道管理器
        contribKit = ServerContribKit.duang(RobotUtil.getServerHost(), RobotUtil.getServerPort());
        LOG.info("车辆[{}]完成Robot适配器初始化完成", getName());
    }

    /**
     * 开启通讯适配器
     */
    @Override
    public synchronized void enable() {
        if (isEnabled()) {
            LOG.info("车辆[{}]已开启通讯适配器，请勿重复开启", getName());
            return;
        }
        try {
            // 每开启一个车辆就启动一个定时监听器
            moveCommandListener = new MoveCommandListener(this);
            moveRequesterTask = new MoveRequesterTask(moveCommandListener);
            moveRequesterTask.enable(getName());
            initVehiclePosition(getName());
            super.enable();
            LOG.info("成功注册车辆[{}]通讯适配器", getName());
        } catch (Exception e) {
            LOG.info("注册车辆[{}]通讯适配器失败: {}", getName(), e.getMessage(), e);
        }
    }

    public synchronized void trigger() {

    }

    /**
     * 初始车辆位置
     *
     * @param name 车辆名称
     */
    public synchronized void initVehiclePosition(String name) {
        RobotContext.getRobotComponents().getVehicleStatus().initVehiclePosition(name);
    }

    /**
     * 是否执行进程操作，车辆移动命令发送前检查
     */
    @Nonnull
    @Override
    public ExplainedBoolean canProcess(@Nonnull List<String> operations) {
        requireNonNull(operations, "operations");
        boolean canProcess = true;
        String reason = "";

        if (!isEnabled()) {
            canProcess = false;
            reason = "通讯适配器没有开启";
        }

        if (canProcess && !isVehicleConnected()) {
            canProcess = false;
            reason = "车辆可能没有连接";
        }

        String vehicleStateName = getProcessModel().getVehicleState().name();
        if (canProcess &&
                LoadState.UNKNOWN.name().equalsIgnoreCase(vehicleStateName)) {
            canProcess = false;
            reason = "车辆负载状态未知";
        }

        boolean loaded = LoadState.FULL.name().equalsIgnoreCase(vehicleStateName);
        final Iterator<String> iterator = operations.iterator();
        while (canProcess && iterator.hasNext()) {
            final String nextOp = iterator.next();
            if (loaded) {
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
            } else if (LoadAction.LOAD.equalsIgnoreCase(nextOp)) {
                loaded = true;
            } else if (LoadAction.UNLOAD.equalsIgnoreCase(nextOp)) {
                canProcess = false;
                reason = "未加载时无法卸载";
            }
        }
        return new ExplainedBoolean(canProcess, reason);
    }

    /**
     * 发送移动命令
     * 当有车辆需要进行交通管制时，会自动将可以移动的MovementCommand对象回调到sendCommand方法，
     * 告诉可以使用的MovementCommand对象，可能回调多次或一次，回调次数是根据OpenTCS的交通管制算法将可运行的路径
     * 添加到getCommandQueue()队列，队列再poll到方法
     * 利用这个回调发送移动命令，将多次回调的MovementCommand再次组装成List集合，
     * 再交由业务逻辑部份处理，生成相应的协议内容发送到车辆
     *
     * @param cmd 移动命令对象
     * @throws IllegalArgumentException
     */
    @Override
    public void sendCommand(MovementCommand cmd) throws IllegalArgumentException {
        cmd = requireNonNull(cmd, "MovementCommand is null");
//        LOG.info("######{}发送移动指令: {}", getName(), cmd.getStep().getPath());
        // 添加到队列
        tempCommandQueue.add(cmd);
        /**
         * 监听器引用队列，处理后发送协议，由于监听定时器是由指定时间间隔执行一次，所以这间隔时间不能设置太少
         * 如果设置间隔时间太少，则有可能导致movementCommandQueue添加队列时没有全部添加完成就执行了发送。
         * 目前默认是1秒执行一次，理论上来说，时间是足够的
         */
        moveCommandListener.quoteCommand(tempCommandQueue);
    }

    @Override
    protected List<VehicleCommAdapterPanel> createAdapterPanels() {
        return new ArrayList<>();
    }


    /**
     * 进程消息
     */
    @Override
    public void processMessage(@Nullable Object object) {
        LOG.info("processMessage: {}",object);
        if (object instanceof SetSpeedMultiplier) {
            SetSpeedMultiplier speedMultiplier = (SetSpeedMultiplier)object;
            if (null != speedMultiplier && speedMultiplier.getMultiplier() == 0) {
                vehicleService.sendCommAdapterCommand(vehicle.getReference(), new SetVehiclePausedCommand(true));
            }
        }
    }

    /**
     * 取车辆进程模型
     */
    @Override
    public final RobotProcessModel getProcessModel() {
        return (RobotProcessModel) super.getProcessModel();
    }

    /**
     * 取移动命令队列
     *
     * @return
     */
    public Queue<MovementCommand> getMovementCommandQueue() {
        return movementCommandQueue;
    }

    /**
     * 检查移动订单是否完成
     *
     * @param stateModel
     */
    private void checkOrderFinished(RobotStateModel stateModel) {
        MovementCommand cmd = getSentQueue().peek();
        if (null == cmd) {
            return;
        }
        String operation = cmd.getOperation();
        // 不是NOP，是最后一条指令并且自定义动作组合里包含该动作名称
        if (null != cmd &&
                !cmd.isWithoutOperation() &&
                cmd.isFinalMovement() &&
                ToolsKit.isNotEmpty(operation)) {
            // 如果动作指令操作未运行则可以运行
            executeLocationActions(cmd, getName(), operation);
        } else {
            LOG.info("车辆[{}]移动到点[{}]成功", getName(), cmd.getStep().getDestinationPoint().getName());
            MovementCommand curCommand = getSentQueue().poll();
            if (null != cmd && null != curCommand && cmd.equals(curCommand)) {
                if (curCommand.isFinalMovement()) {
                    //车辆设置为空闲状态，执行下一个移动指令
                    getProcessModel().setVehicleState(Vehicle.State.IDLE);
                    // 取消单步执行状态
                    getProcessModel().setSingleStepModeEnabled(false);
                }
                getProcessModel().commandExecuted(curCommand);
            }
        }
    }

    /**
     * 执行工站动作指令
     *
     * @param cmd
     * @param adapterName 通讯适配器名称
     * @param operation   工站动作指令集名称
     */
    private void executeLocationActions(MovementCommand cmd, String adapterName, String operation) {

        if (null == cmd && !operation.equalsIgnoreCase(cmd.getOperation())) {
            throw new RobotException("车辆[" + adapterName + "]通讯适配器移动命令为null或工站指令集名称[" + operation + "!=" + cmd.getOperation() + "]");
        }

        if (!RobotUtil.isContainActionsKey(operation)) {
            throw new RobotException("调度系统没有发现指定的工站动作名称: " + operation + ", 请检查是否正确设置，工站名称须唯一且一致");
        }
        if (!isEnabled()) {
            LOG.error("车辆[{}]通讯适配器没开启，请先开启！", adapterName);
            return;
        }
        // 需要判断是否已经执行自定义指令，如果正在执行，则退出。
        if (executeLocationActionNameSet.contains(operation)) {
            LOG.error("车辆[{}]已经执行自定义指令集合[{}]操作，不能重复执行，请检查！", adapterName, operation);
            return;
        }
        LOG.info("车辆[{}]开始执行自定义指令集合[{}]操作", adapterName, operation);
        try {
            //设置为执行状态
            getProcessModel().setVehicleState(Vehicle.State.EXECUTING);
            // 设置为允许单步执行，即等待自定义命令执行完成或某一指令取消单步操作模式后，再发送移动车辆命令。
            getProcessModel().setSingleStepModeEnabled(true);
            // 线程执行自定义指令队列
            ThreadUtil.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        IAction action = RobotUtil.getLocationAction(operation);
                        if (ToolsKit.isNotEmpty(action)) {
                            // 添加到执行动作集合，标识该动作已经执行，执行完成后，需要移除
                            executeLocationActionNameSet.add(operation);
                            // 调用BaseActions里execute方法
                            action.execute();
                        } else {
                            LOG.info("根据[{}]查找不到对应的动作指令处理类", operation);
                        }
                    } catch (Exception e) {
                        LOG.error("执行自定义动作组合指令时出错: " + e.getMessage(), e);
                    }
                }
            });
        } catch (Exception e) {
            throw new RobotException(e.getMessage(), e);
        }
    }

    /***
     * BaseActios执行指令集完成后，调用该方法，执行下一个订单
     * @param vehicleId 车辆ID
     * @param operation 工站自定义动作名称
     */
    public void executeNextMoveCmd(String vehicleId, String operation) {
        if (!getName().equals(vehicleId)) {
            throw new RobotException("工站提交过来的车辆ID["+vehicleId+"]与适配器["+getName()+"]不匹配，请检查！");
        }
        LOG.info("检查车辆[{}]是否有下一个移动订单，如有则继续执行！", getName());
        RobotProcessModel processModel = getProcessModel();
        //车辆设置为空闲状态，执行下一个移动指令
        getProcessModel().setVehicleState(Vehicle.State.IDLE);
        // 取消单步执行状态
        getProcessModel().setSingleStepModeEnabled(false);
        // 移除移动命令
        MovementCommand cmd = getSentQueue().poll();
        // 则executeLocationActionNameSet里的动作指令移除，允许再次执行
        if(!executeLocationActionNameSet.contains(operation)) {
            throw new RobotException("工站动作名称["+operation+"]不存在于车辆适配器["+getName()+"]，请检查！");
        }
        executeLocationActionNameSet.remove(operation);
        // 如果不为空且是最终移动命令，则执行下一个订单
        if (null != cmd && cmd.isFinalMovement()) {
            processModel.commandExecuted(cmd);
        }
    }

    /**
     * 覆盖实现
     * 用于将值传递到控制中心的自定义面板
     * 启动时，面板点击更新后均会触发
     *
     * @return
     */
    @Override
    protected RobotVehicleModelTO createCustomTransferableProcessModel() {
        // 发送到其他软件（如控制中心或工厂概览）时，添加车辆的附加信息
        return new RobotVehicleModelTO()
                .setSingleStepModeEnabled(getProcessModel().isSingleStepModeEnabled())
                .setLoadOperation(getProcessModel().getLoadOperation())
                .setUnloadOperation(getProcessModel().getUnloadOperation())
                .setMaxAcceleration(getProcessModel().getMaxAcceleration())
                .setMaxDeceleration(getProcessModel().getMaxDecceleration())
                .setMaxFwdVelocity(getProcessModel().getMaxFwdVelocity())
                .setMaxRevVelocity(getProcessModel().getMaxRevVelocity())
                .setOperatingTime(getProcessModel().getOperatingTime())
                .setVehiclePaused(getProcessModel().isVehiclePaused());
    }

    /**
     * 通知业务处理模板，订单已经完成
     */
    public void noticeOrderFinished() {
        if (getCommandQueue().isEmpty() && getSentQueue().isEmpty()) {
            moveCommandListener.noticeOrderFinished();
        }
    }

    /***************************************BasicVehicleCommAdapter 抽象方法************************************************/
    /**
     * 连接车辆，在BasicVehicleCommAdapter里enable方法下回调
     */
    @Override
    protected void connectVehicle() {

        if (null == contribKit) {
            LOG.warn("车辆[{}]通讯渠道管理器不存在", getName());
            return;
        }
        // 根据车辆设置的host与port，连接车辆
        String name = getName();
        String host = RobotUtil.getVehicleHost(name);
        int port = RobotUtil.getVehiclePort(name);
        try {
            contribKit.register(name, host, port, this);
            LOG.info("注册车辆[{}]成功: [{}]", name, (host + ":" + port));

            //如果是TCP，则根据在控制地图里，对车辆属性表输入的json数据进行设备注册
            List<DeviceAddress> deviceAddressList = getProcessModel().getDeviceAddress();
            if (NetChannelType.TCP.equals(RobotUtil.getNetChannelType()) && ToolsKit.isNotEmpty(deviceAddressList)) {
                for(Iterator<DeviceAddress> iterator = deviceAddressList.iterator(); iterator.hasNext();) {
                    DeviceAddress deviceAddress = iterator.next();
                    name = deviceAddress.getName();
                    host = deviceAddress.getHost();
                    port = deviceAddress.getPort();
                    contribKit.register(name, host, port, this);
                    LOG.info("注册设备[{}]成功: [{}]", name, (host + ":" + port));
                }
            } // 如果UDP或RXTX的话，就根据
            else {
                EntryName entryName = RobotUtil.getEntryName(getName());
                try {
                    for (Iterator<String> iterator = entryName.getDeviceNameList().iterator(); iterator.hasNext(); ) {
                        String deviceName = iterator.next();
                        contribKit.register(deviceName, host, port, this);
                        LOG.info("注册设备[{}]成功: [{}]", deviceName, (host + ":" + port));
                    }
                } catch (Exception e) {
                    throw new RobotException("注册设备[{}]失败，请注意是否添加了车辆Service类！");
                }
            }


        } catch (RobotException e) {
            LOG.error("连接或注册车辆或设备[{}]时发生异常: {}", name, e.getMessage());
            throw e;
        }
    }

    /**
     * 断开车辆连接
     */
    @Override
    protected void disconnectVehicle() {
        if (null == contribKit) {
            LOG.warn("车辆[{}]通讯渠道管理器不存在.", getName());
            return;
        }
        try {
            contribKit.closeConnection(getName());
            // 清除与该车辆相关的参数
            getSentQueue().clear();
            getCommandQueue().clear();
            moveCommandListener = null;
            moveRequesterTask.disable(getName());
            getProcessModel().setCommAdapterConnected(false);
            getProcessModel().setVehicleIdle(true);
            getProcessModel().setVehicleState(Vehicle.State.UNKNOWN);
            LOG.info("成功断开车辆[{}]通讯适配器链接", getName());
        } catch (Exception e) {
            LOG.error("断开车辆[{}]通讯适配器链接时发生异常: {}", getName(), e.getMessage());
            throw e;
        }
    }

    /**
     * 判断车辆是否已经连接
     */
    @Override
    protected boolean isVehicleConnected() {
        if (null != contribKit) {
            return contribKit.isConnected(getName());
        }
        return false;
    }


    /*******************************ConnectionEventListener  接口方法开始**************************************/
    /**
     * 接收到报文信息，此次报文指令应是车辆上报卡号的指令协议
     * 上报卡号后，opentcs也应该同步更新UI界面以显示车辆最新位置
     *
     * @param stateModel 状态对象
     */
    @Override
    public void onIncomingTelegram(RobotStateModel stateModel) {
        requireNonNull(stateModel, "stateModel");
        // 车辆状态设置为不空闲
        getProcessModel().setVehicleIdle(false);
        // 当前位置,上报的位置
        String currentReportPosition = stateModel.getCurrentPosition();
        if (ToolsKit.isEmpty(currentReportPosition)) {
            throw new RobotException("更新位置不能为空！");
        }
        try {
            /**
             *每上报一个卡号，比较上报的卡号与队列中的第1位元素是否匹配，匹配则将第一位的元素移除，否则抛出异常，发送停车协议
             * 匹配规则：比较上报的卡号与队列中的第一位是否相等 ,如果不一致，则抛出异常，让业务逻辑代码作后续处理，例如立即停车
             */
            if (!getMovementCommandQueue().isEmpty()) {
                // 比较卡号是否与队列中的第1位元素一致
                MovementCommand command = getMovementCommandQueue().peek();
                if (null != command) {
                    String destinationPointName = command.getStep().getDestinationPoint().getName();
                    if (null == destinationPointName) {
                        throw new RobotException("适配器[" + getName() + "]移动命令中的目标点名称不能为空！");
                    }
                    // 如果目标点与上报的点不一致，则取起始点再进行比较
                    if (!destinationPointName.equals(currentReportPosition)) {
                        String sourcePointName = command.getStep().getSourcePoint().getName();
                        if (sourcePointName.equals(currentReportPosition)) {
                            LOG.info("由于上报位置[{}]是车辆起始位置，适配器[{}]将忽略该上报请求！", currentReportPosition, getName());
                            return;
                        }
                    }
                    if (destinationPointName.equals(currentReportPosition)) {
                        getMovementCommandQueue().remove();
                        LOG.info("车辆[{}]接收到的上报位置[{}]与车辆移动指令队列中的第1位元素一致，移除后继续执行操作!", getName(), currentReportPosition);
                    } else {
                        throw new RobotException("车辆[" + getName() + "]接收到的上报位置[" + currentReportPosition + "]与车辆移动指令队列中的第1位元素[" + destinationPointName + "]不一致，请检查！");
                    }
                }
            }
            // 根据上报的卡号，更新位置
            getProcessModel().setVehiclePosition(currentReportPosition);
            // 更新为最新状态
            getProcessModel().setVehicleState(RobotUtil.translateVehicleState(stateModel.getOperatingState()));
            //  检查移动订单是否完成
            checkOrderFinished(stateModel);
        } catch (Exception e) {
            LOG.error("vehicle[" + getName() + "] adapter onIncomingTelegram is exception: " + e.getMessage(), e);
            throw new RobotException(e.getMessage(), e);
        }
    }

    /**
     * 链接车辆
     */
    @Override
    public void onConnect() {
        getProcessModel().setCommAdapterConnected(true);
        LOG.info("netty回调事件: 车辆[{}]连接成功", getName());
    }

    /***/
    @Override
    public void onFailedConnectionAttempt() {
        if (!isEnabled()) {
            return;
        }
        getProcessModel().setCommAdapterConnected(false);
        if (isEnabled() &&
                getProcessModel().isReconnectingOnConnectionLoss()) {
            if (contribKit.isConnected(getName())) {
                contribKit.closeConnection(getName());
            }
        }
    }

    /**
     * 断开链接
     */
    @Override
    public void onDisconnect() {
        disconnectVehicle();
        if (isEnabled() &&
                getProcessModel().isReconnectingOnConnectionLoss() &&
                RobotUtil.isClientRunType()) {
            if (contribKit.isConnected(getName())) {
                contribKit.closeConnection(getName());
            }
        }
    }

    /**
     * 空闲
     */
    @Override
    public void onIdle() {
        getProcessModel().setVehicleIdle(true);
        LOG.debug("netty回调事件，车辆[{}]空闲", getName());
        // 如果支持重连则的车辆空闲时断开连接
        if (isEnabled() && getProcessModel().isDisconnectingOnVehicleIdle()) {
            LOG.debug("车辆[{}]开启了空闲时断开连接", getName());
            disconnectVehicle();
        }
    }

    /********************************* ITelegramSender *************************************/
    /**
     * 发送报文
     *
     * @param response 电报响应对象
     */
    @Override
    public void sendTelegram(IResponse response) {
        if (null == contribKit) {
            throw new RobotException("contribKit is null");
        }
        try {
            contribKit.send(getName(), response.getRawContent());
        } catch (Exception e) {
            LOG.error("发送报文消息到[{}]时异常: {}", getName(), e.getMessage(), e);
        }
    }
}
