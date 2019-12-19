package com.openagv.opentcs.adapter;

import cn.hutool.core.thread.ThreadUtil;
import com.google.inject.assistedinject.Assisted;
import com.openagv.core.AppContext;
import com.openagv.core.interfaces.*;
import com.openagv.opentcs.enums.LoadAction;
import com.openagv.opentcs.enums.LoadState;
import com.openagv.opentcs.model.ProcessModel;
import com.openagv.opentcs.model.VehicleModelTO;
import com.openagv.opentcs.telegrams.StateRequest;
import com.openagv.opentcs.telegrams.StateRequesterTask;
import com.openagv.opentcs.telegrams.TelegramMatcher;
import com.openagv.plugins.serialport.SerialPortManager;
import com.openagv.plugins.udp.UdpServerChannelManager;
import com.openagv.tools.ToolsKit;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.apache.log4j.Logger;
import org.opentcs.components.kernel.services.TCSObjectService;
import org.opentcs.contrib.tcp.netty.TcpClientChannelManager;
import org.opentcs.data.model.Vehicle;
import org.opentcs.data.order.DriveOrder;
import org.opentcs.data.order.Route;
import org.opentcs.drivers.vehicle.BasicVehicleCommAdapter;
import org.opentcs.drivers.vehicle.MovementCommand;
import org.opentcs.util.ExplainedBoolean;


import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Consumer;

import static java.util.Objects.requireNonNull;

/**
 * 通讯适配器
 *
 * @author Laotang
 */
public class CommAdapter extends BasicVehicleCommAdapter {

    private static final Logger logger = Logger.getLogger(CommAdapter.class);
    // 组件工厂
    private ComponentsFactory componentsFactory;

    // 车辆管理缓存池
    private Object channelManager;
    private TcpClientChannelManager<String, String> tcpClientChannelManager;
    private UdpServerChannelManager<String,String> udpServerChannelManager;
    private SerialPortManager serialPortManager;

    // 请求响应电报匹配器
    private TelegramMatcher telegramMatcher;
    // 定时发送任务器
    private StateRequesterTask stateRequesterTask;

    //面板中的下一步控制开发
    private boolean singleStepExecutionAllowed = false;

    // 该条线路所有StateRequest，将所有指令整合成一条返回
    private final static Map<String, LinkedBlockingQueue<MovementCommand>> commandQueueMap = new ConcurrentHashMap<>();
    // 车辆与工站的关系，key为车辆ID，value为移动命令最后一条指令，即工站所在的点
    private final static Map<String, MovementCommand> finalCommandMap = new ConcurrentHashMap<>();
    private TCSObjectService objectService;

    /**
     * 自定义动作是否运行
     * 如果key存在，则正在运行该指定的动作组合
     */
    private final static Map<String,String>  CUSTOM_ACTIONS_MAP = new java.util.concurrent.ConcurrentHashMap<>();

    /***
     * 构造函数
     * @param vehicle   车辆
     * @param componentsFactory 组件工厂
     */
    @Inject
    public CommAdapter(@Assisted Vehicle vehicle, TCSObjectService objectService, ComponentsFactory componentsFactory) {
        /**
         *commandQueueCapacity: 此通信适配器的命令队列接受的命令数。必须至少为1。
         * sentQueueCapacity: 要发送给车辆的最大订单数。
         * 设置为100，即允许可以执行100个点的线路
         */
        super(new ProcessModel(vehicle), ToolsKit.getCommandQueueCapacity(), ToolsKit.getSentQueueCapacity(), LoadAction.CHARGE);
        this.componentsFactory = requireNonNull(componentsFactory, "componentsFactory");
        this.objectService = requireNonNull(objectService, "objectService");
        AppContext.setCommAdapter(this);
    }

    public TCSObjectService getObjectService() {
        java.util.Objects.requireNonNull(objectService, "objectService is null");
        return objectService;
    }

    public TelegramMatcher getTelegramMatcher(){
        java.util.Objects.requireNonNull(telegramMatcher, "telegramMatcher is null");
        return telegramMatcher;
    }

    /***
     *  初始化
     */
    @Override
    public void initialize() {
        super.initialize();
    }

    /**
     * 终止
     */
    @Override
    public void terminate() {
        super.terminate();
    }

    private boolean channelManagerEnabled() {
        if(ToolsKit.isNotEmpty(channelManager)) {
            if(channelManager instanceof TcpClientChannelManager) {
                TcpClientChannelManager tcpClientChannelManager = (TcpClientChannelManager) channelManager;
                return tcpClientChannelManager.isConnected();
            }
            else if(channelManager instanceof UdpServerChannelManager) {
                UdpServerChannelManager udpServerChannelManager = (UdpServerChannelManager) channelManager;
                return udpServerChannelManager.isConnected();
            }
            else if(channelManager instanceof SerialPortManager) {
                SerialPortManager serialPortManager = (SerialPortManager) channelManager;
                return serialPortManager.isConnected();
            }
        }
        return false;
    }

    /**
     * 内核控制中心列表enable列勾选复选框后触发
     */
    @Override
    public synchronized void enable() {
        // 如果启用了则直接退出
        if (isEnabled()) {
            return;
        }
        // 判断是否重复开启
        if (channelManagerEnabled()) {
            return;
        }
        getProcessModel().getVelocityController().addVelocityListener(getProcessModel());
        logger.info("初始化车辆渠道管理器");
        // 初始化车辆渠道管理器
        for(Iterator<IEnable> iterator = AppContext.getPluginEnableList().iterator(); iterator.hasNext();){
            IEnable enable = iterator.next();
            channelManager = enable.enable();

            if (null == channelManager) {
                return;
            }

            // 开启链接或监听
            if(channelManager instanceof TcpClientChannelManager) {
                tcpClientChannelManager = (TcpClientChannelManager) channelManager;
            }
            else if(channelManager instanceof UdpServerChannelManager) {
                udpServerChannelManager = (UdpServerChannelManager) channelManager;
            }
            else if(channelManager instanceof SerialPortManager) {
                serialPortManager = (SerialPortManager) channelManager;
            }

            // 回调发送消息
            if(ToolsKit.isEmpty(telegramMatcher)) {
                this.telegramMatcher = new TelegramMatcher((ITelegramSender) enable);
            }
            // 定时任务
            if(ToolsKit.isNotEmpty(telegramMatcher)) {
                IHandshakeListener listener = AppContext.getAgvConfigure().getHandshakeListener();
                if(ToolsKit.isNotEmpty(listener)) {
                    listener.setSender(telegramMatcher.getTelegramSender());
                    this.stateRequesterTask = new StateRequesterTask(listener);
                    stateRequesterTask.enable();
                }
            }
            logger.info("注册回调发送消息成功");
        }
        // 调用父类开启
        super.enable();
        AppContext.setCommAdapter(this);
        AppContext.getAgvConfigure().getConnectionEventListener().onConnect();
    }

    /**
     * 车辆进程参数模型
     * @return
     */
    @Override
    public final ProcessModel getProcessModel() {
        return (ProcessModel) super.getProcessModel();
    }

    public TcpClientChannelManager<String, String> getTcpClientChannelManager() {
        return tcpClientChannelManager;
    }
    public UdpServerChannelManager<String, String> getUdpServerChannelManager() {
        return udpServerChannelManager;
    }
    public SerialPortManager getSerialPortManager() {
        return serialPortManager;
    }

    // 面板中的下一步按钮
    public synchronized void nextStepButton() {
        singleStepExecutionAllowed = true;
    }

    /**
     * 重写父类里的是否可以发送下一条指令
     *
     * 已发送的命令数小于车辆的容量，并且队列中至少有一个命令正在等待发送
     * 并且不是单步模式
     * @return true可以发送
     */
    @Override
    protected synchronized boolean canSendNextCommand() {
        return super.canSendNextCommand() && (!getProcessModel().isSingleStepModeEnabled() || singleStepExecutionAllowed);
//        boolean isCanSendNextCommand =  super.canSendNextCommand() && (!getProcessModel().isSingleStepModeEnabled() || singleStepExecutionAllowed);
//        logger.info("canSendNextCommand："+ isCanSendNextCommand);
//        return isCanSendNextCommand;
    }

    /**
     * 控制中心完成运输订单设置后，在车辆每移动一个点时，会执行以下方法
     * @param cmd 车辆移动相关参数
     * @throws IllegalArgumentException
     */
    @Override
    public void sendCommand(MovementCommand cmd) throws IllegalArgumentException {
        requireNonNull(cmd, "移动命令不能为空");
        singleStepExecutionAllowed = false;
        String key = getProcessModel().getName();
        logger.info(key + "     sendCommand:" + cmd);
        LinkedBlockingQueue<MovementCommand> commandQueue = commandQueueMap.get(key);
        if (null == commandQueue) {
            commandQueue = new LinkedBlockingQueue<>();
        }
        commandQueue.add(cmd);
        commandQueueMap.put(key, commandQueue);
        // 如果是交通管制，则生成出来的路径协议就是两个点两点的下发
        boolean isTrafficControl = ToolsKit.isTrafficControl(getProcessModel());
        if (isTrafficControl) {
            logger.info("该车辆需要进行交通管理，路径为单步发送");
            sendStateRequest(key, cmd);
        } else if (cmd.isFinalMovement()) {
            logger.info("该车辆不需要进行交通管理，路径为全部发送");
            finalCommandMap.put(key, cmd); // is final movecommand
            sendStateRequest(key, cmd);
        }
    }

    /**
     * 发送车辆移动请求
     * @param cmd
     */
    private void  sendStateRequest(String vehicleName, MovementCommand cmd) {
        LinkedBlockingQueue<MovementCommand> queue = commandQueueMap.get(vehicleName);
        if (null == queue || queue.isEmpty()) {
            logger.info("命令对列不能为空，退出生成协议方法");
            return;
        }
        try {
            // 将移动的参数转换为请求返回参数，这里需要调用对应的业务逻辑根据协议规则生成对应的请求返回对象
            IResponse response = ToolsKit.sendCommand(
                    new StateRequest.Builder()
                            .commandQuery(queue)
                            .finalCmd(cmd)
                            .model(getProcessModel())
                            .build());
            if (response.getStatus() != HttpResponseStatus.OK.code()) {
                telegramMatcher.getTelegramSender().sendTelegram(response);
                throw new IllegalArgumentException(response.toString());
            }
            // 把请求加入队列。请求发送规则是FIFO。这确保我们总是等待响应，直到发送新请求。
            telegramMatcher.enqueueRequestTelegram(response);
            logger.info(getName() + ": 将车辆移动报文提交到消息队列完成");
        } catch (Exception e) {
            logger.error(getName() + "构建指令或将订单报文提交到消息队列失败: " + e.getMessage(), e);
        } finally {
            queue.clear(); //成功失败都需要清空该命令队列对象
        }
    }


    /**
     * 清理命令队列
     */
    @Override
    public synchronized void clearCommandQueue() {
        AppContext.getAgvConfigure().getHandshakeTelegramQueue().clearQueue();
        logger.error("清除握手队列成功");
        CUSTOM_ACTIONS_MAP.clear();
        super.clearCommandQueue();
        getProcessModel().setSingleStepModeEnabled(false);
        logger.info("###########clearCommandQueue");
    }

    /***
     * 链接车辆
     */
    @Override
    protected void connectVehicle() {
        logger.info("###########connectVehicle");
        // TODO 可以改为下拉选择的方式 ，待完成，目前先将起点位置设置为Point-0001
//        getProcessModel().setVehiclePosition("36");
//        getProcessModel().setVehiclePosition("1");
//        getProcessModel().setVehiclePosition("50");
//        getProcessModel().setVehiclePosition("237");
//        getProcessModel().setVehiclePosition("Point-0001");
//        Point point = ToolsKit.getPoint("Point-0001");
//        point.setVehicleOrientationAngle(-90);


//        String vehicleName = getProcessModel().getName();
//        if ("A001".equals(vehicleName)) {
////            getProcessModel().setVehiclePosition("218");
//            getProcessModel().setVehiclePosition("223");
//        }
//        else if ("A002".equals(vehicleName)) {
////            getProcessModel().setVehiclePosition("213");
//            getProcessModel().setVehiclePosition("230");
//        }
//        else if ("A033".equals(vehicleName)) {
//            getProcessModel().setVehiclePosition("50");
//        }


        getProcessModel().setVehicleState(Vehicle.State.IDLE);
        getProcessModel().setVehicleIdle(true);

        if(ToolsKit.isNotEmpty(serialPortManager)) {
            logger.warn("串口模式是以广播方式发送，退出connectVehicle方法");
            return;
        }

        if(ToolsKit.isNotEmpty(udpServerChannelManager)) {
            logger.warn("UDP模式不需要进行握手，退出connectVehicle方法");
            return;
        }

        if (ToolsKit.isEmpty(tcpClientChannelManager)) {
            logger.warn("连接车辆 "+getName()+" 时失败: vehicleChannelManager not present.");
            return;
        }
        /**进行bind操作*/
        String host = getProcessModel().getVehicleHost();
        int port = getProcessModel().getVehiclePort();
        tcpClientChannelManager.connect(host, port);
        logger.warn("连接车辆 "+getName()+" 成功:  host:"+host+" port: "+ port);

    }

    /**
     * 断开连接
     */
    @Override
    protected void disconnectVehicle() {
        logger.info("###########disconnectVehicle");
        try {
            // 将相关的对象清空
            if (channelManager instanceof TcpClientChannelManager) {
                tcpClientChannelManager.disconnect();
                tcpClientChannelManager = null;
                logger.warn("断开连接车辆 " + getName() + " 时失败: vehicleChannelManager not present.");
            } else if (channelManager instanceof UdpServerChannelManager) {
                udpServerChannelManager.disconnect();
                udpServerChannelManager = null;
            } else if (channelManager instanceof SerialPortManager) {
                serialPortManager.closePort(AppContext.getSerialPort());
                serialPortManager = null;
            }
            channelManager = null;
            telegramMatcher = null;
            // TODO 对当前车辆停止，是否要全部定时器也停？
            stateRequesterTask.disable();
            // 清除命令队列
            clearCommandQueue();
            logger.warn("断开车辆连接 " + getName() + " 成功");
        } catch (Exception e) {
            logger.warn("断开车辆链接 " + getName() + " 时失败: " + e.getMessage(), e);
        }
    }

    /**
     * 判断车辆连接是否断开
     * @return  链接状态返回true
     */
    @Override
    public boolean isVehicleConnected() {
        logger.info("######################: isVehicleConnected");
        if(null != tcpClientChannelManager && (tcpClientChannelManager instanceof TcpClientChannelManager)) {
            return tcpClientChannelManager.isConnected();
        }
        else  if(null != serialPortManager && (serialPortManager instanceof  SerialPortManager)) {
            return serialPortManager.isConnected();
        }
        else if(null != udpServerChannelManager && (udpServerChannelManager instanceof UdpServerChannelManager)) {
            return udpServerChannelManager.isConnected();
        }
        return false;
    }


    /**
     * 创建运输订单后执行该方法
     * @param operations    操作标识字符串  例如 NOP
     * @return
     */
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
        logger.info("canProcess: "+operations+", reason: " + reason);
        return new ExplainedBoolean(canProcess, reason);

    }

    @Override
    public void processMessage(@Nullable Object o) {
        logger.info("#################processMessage: "+ o);
    }

    /**
     *车辆位置更新并执行自定义指令
     * @param response 车辆回复的协议
     */
    public void updateVehiclePositionAndExecuteCmd(IResponse response) {
        // 将报告的位置ID映射到点名称
        List<String> currentPositionList = response.getNextPointNames();
        String deviceId = response.getDeviceId();
        logger.info(deviceId + " 更新车辆位置"+ToolsKit.toJsonString(currentPositionList)+"并执行自定义动作指令");
        String postCurrentPoint = ToolsKit.isNotEmpty(currentPositionList ) ? currentPositionList.get(0) : "";
        if(ToolsKit.isEmpty(postCurrentPoint)) {
            logger.info("currentPositionList empty is " + ToolsKit.isNotEmpty(currentPositionList )+"      postCurrentPoint is empty, so exit function......");
            return;
        }
        AppContext.getKernelServicePortal().
                getVehicleService().
                fetchProcessModel(ToolsKit.getVehicle(deviceId).getReference())
                .setVehiclePosition(postCurrentPoint);
        logger.info("Vehicle[" +deviceId + "] move to " + postCurrentPoint+ " point is success!");
        // Update GUI.
        synchronized (CommAdapter.this) {
            MovementCommand currentCmd = ToolsKit.isTrafficControl(deviceId) ? getSentQueue().peek() : finalCommandMap.get(response.getDeviceId());
            if (currentCmd == null) {
                logger.info("根据车辆["+deviceId+"]取点["+postCurrentPoint+"]的MovementCommand对象为空！退出处理...");
                return;
            }
            Route.Step step = currentCmd.getStep();
            String sourcePointName = step.getSourcePoint().getName();
            System.out.println("###############: "  + currentCmd.isFinalMovement() +"                 "+sourcePointName+"             "+ currentCmd.getFinalDestination().getName());
            //到达最终停车点后判断是否有自定义操作，如果有匹配的标识符，则执行自定义操作
            if(postCurrentPoint.equals(sourcePointName) &&
                    !currentCmd.isWithoutOperation() &&
                    currentCmd.isFinalMovement()  &&
                    isContainActionsKey(currentCmd)) {
                // 如果动作指令操作未运行则可以运行
                String operation = currentCmd.getOperation();
                if (!CUSTOM_ACTIONS_MAP.containsKey(operation)) {
                    logger.info("车辆["+response.getDeviceId()+"]对应位置["+postCurrentPoint+"]上的设备开始执行动作["+operation+"]");
                    executeCustomCmds(response.getDeviceId(), operation);
                } else {
                    logger.info("不能重复执行该操作，因该动作指令已经运行，作丢弃处理！");
                }
            } else {
                executeNextMoveCmd(null);
            }
            // 唤醒处于等待状态的线程
            CommAdapter.this.notify();
        }
    }

    /**
     * 最后执行的动作名称是否包含自定义模板集合中
     * @param currentCmd
     * @return
     */
    private boolean isContainActionsKey(MovementCommand currentCmd) {
        String operation = currentCmd.getOperation();
        IAction actionTemplate = AppContext.getCustomActionsQueue().get(operation);
        if(ToolsKit.isEmpty(actionTemplate)) {
            actionTemplate = AppContext.getCustomActionsQueue().get(operation.toUpperCase());
            if(ToolsKit.isEmpty(actionTemplate)) {
                actionTemplate = AppContext.getCustomActionsQueue().get(operation.toLowerCase());
            }
        }
        if(ToolsKit.isEmpty(actionTemplate)) {
            logger.info("请先配置需要执行的自定义指令组合，名称需要一致，不区分大小写");
            return false;
        }
        return true;
    }

    /**
     * 执行自定义指令组合
     * @param operations 指令组合标识字符串
     */
    private void executeCustomCmds(String vehicleName, String operations)  {
        final String operation = requireNonNull(operations, "operation is null");
        if (!isEnabled()) {
            return ;
        }
        logger.info(vehicleName+": 开始执行自定义指令集合["+operation+"]操作");
        try {
            //设置为执行状态
            getProcessModel().setVehicleState(Vehicle.State.EXECUTING);
            // 设置为允许单步执行，即等待自定义命令执行完成或某一指令取消单步操作模式后，再发送移动车辆命令。
            getProcessModel().setSingleStepModeEnabled(true);
            // 执行自定义指令队列
            ThreadUtil.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        AppContext.getCustomActionsQueue().get(operation).execute();
                    } catch (Exception e) {
                        logger.error("执行自定义动作组合指令时出错: " + e.getMessage(), e);
                    }
                }
            });
            CUSTOM_ACTIONS_MAP.put(operation, operation);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    /**
     * 执行下一步移动车辆
     */
    public void executeNextMoveCmd(String actionKey) {
        logger.info("成功执行自定义指令完成，则检查是否有下一订单，如有则继续执行");
        //车辆设置为空闲状态，执行下一个移动指令
        getProcessModel().setVehicleState(Vehicle.State.IDLE);
        // 取消单步执行状态
        getProcessModel().setSingleStepModeEnabled(false);
        MovementCommand cmd = getSentQueue().poll();
//        System.out.println("cmd.getStep().getSourcePoint(): " + cmd.getStep().getSourcePoint());
        getProcessModel().commandExecuted(cmd);
        //移除指定动作的名称
        if(ToolsKit.isNotEmpty(actionKey)) {
            CUSTOM_ACTIONS_MAP.remove(actionKey);
        }
    }


    /**
     * 覆盖实现
     * 用于将值传递到控制中心的自定义面板
     * 启动时，面板点击更新后均会触发
     * @return
     */
    @Override
    protected VehicleModelTO createCustomTransferableProcessModel() {
        // 发送到其他软件（如控制中心或工厂概览）时，添加车辆的附加信息
        return new VehicleModelTO()
                .setLoadOperation(getProcessModel().getLoadOperation())
                .setMaxAcceleration(getProcessModel().getMaxAcceleration())
                .setMaxDeceleration(getProcessModel().getMaxDecceleration())
                .setMaxFwdVelocity(getProcessModel().getMaxFwdVelocity())
                .setMaxRevVelocity(getProcessModel().getMaxRevVelocity())
                .setOperatingTime(getProcessModel().getOperatingTime())
                .setSingleStepModeEnabled(getProcessModel().isSingleStepModeEnabled())
                .setUnloadOperation(getProcessModel().getUnloadOperation())
                .setVehiclePaused(getProcessModel().isVehiclePaused());
    }

}


