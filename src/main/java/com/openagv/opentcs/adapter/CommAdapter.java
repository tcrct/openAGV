package com.openagv.opentcs.adapter;

import com.google.inject.assistedinject.Assisted;
import com.openagv.core.AppContext;
import com.openagv.core.interfaces.*;
import com.openagv.opentcs.enums.LoadAction;
import com.openagv.opentcs.enums.LoadState;
import com.openagv.opentcs.model.ProcessModel;
import com.openagv.opentcs.telegrams.StateRequest;
import com.openagv.opentcs.telegrams.StateRequesterTask;
import com.openagv.opentcs.telegrams.TelegramMatcher;
import com.openagv.opentcs.telegrams.TelegramQueueDto;
import com.openagv.plugins.serialport.SerialPortManager;
import com.openagv.plugins.udp.UdpServerChannelManager;
import com.openagv.tools.ToolsKit;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.apache.log4j.Logger;
import org.opentcs.components.kernel.services.TCSObjectService;
import org.opentcs.contrib.tcp.netty.TcpClientChannelManager;
import org.opentcs.data.model.Vehicle;
import org.opentcs.data.order.DriveOrder;
import org.opentcs.drivers.vehicle.BasicVehicleCommAdapter;
import org.opentcs.drivers.vehicle.MovementCommand;
import org.opentcs.util.ExplainedBoolean;


import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;

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

    private boolean singleStepExecutionAllowed = false;

    private final Map<MovementCommand, String> commandMap = new ConcurrentHashMap<>();

    private TCSObjectService objectService;

    /***
     * 构造函数
     * @param vehicle   车辆
     * @param componentsFactory 组件工厂
     */
    @Inject
    public CommAdapter(@Assisted Vehicle vehicle, TCSObjectService objectService, ComponentsFactory componentsFactory) {
        super(new ProcessModel(vehicle), 3, 2, LoadAction.CHARGE);
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

    /**
     * 内核控制中心列表enable列勾选复选框后触发
     */
    @Override
    public synchronized void enable() {
        // 如果启用了则直接退出
        if (isEnabled()) {
            return;
        }
        getProcessModel().getVelocityController().addVelocityListener(getProcessModel());
        logger.info("初始化车辆渠道管理器");
        // 初始化车辆渠道管理器
        for(Iterator<IEnable> iterator = AppContext.getPluginEnableList().iterator(); iterator.hasNext();){
            IEnable enable = iterator.next();
            channelManager = enable.enable();
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

//    public TelegramMatcher getTelegramMatcher() {
//        return telegramMatcher;
//    }
//
//    public AgreementTemplate getTemplate() {
//        return template;
//    }


    public synchronized void trigger() {
        stateRequesterTask.disable();
        singleStepExecutionAllowed = true;
    }
    /**
     * 是否可以发送下一条指令
     * @return true可以发送
     */
    @Override
    protected synchronized boolean canSendNextCommand() {
        boolean isCanSendNextCommand =  super.canSendNextCommand()
                && (!getProcessModel().isSingleStepModeEnabled() || singleStepExecutionAllowed);

        logger.debug("canSendNextCommand: " + isCanSendNextCommand);
        return isCanSendNextCommand;
    }

    /**
     * 控制中心完成运输订单设置后，在车辆每移动一个点时，会执行以下方法
     * @param cmd 车辆移动相关参数
     * @throws IllegalArgumentException
     */
    @Override
    public void sendCommand(MovementCommand cmd) throws IllegalArgumentException {
        requireNonNull(cmd, "cmd");
        logger.info("sendCommand:" + cmd);
        singleStepExecutionAllowed = false;
        try {
            // 将移动的参数转换为请求返回参数，这里需要调用对应的业务逻辑根据协议规则生成对应的请求返回对象
            IResponse response = ToolsKit.sendCommand(
                    new StateRequest.Builder()
                            .command(cmd)
                            .model(getProcessModel())
                            .build());
            if(response.getStatus() != HttpResponseStatus.OK.code()) {
                throw new IllegalArgumentException(response.toString());
            }
            // 将移动命令放入缓存池
            commandMap.put(cmd, response.getRequestId());
            // 把请求加入队列。请求发送规则是FIFO。这确保我们总是等待响应，直到发送新请求。
            telegramMatcher.enqueueRequestTelegram(response);
            logger.debug(getName()+": 将订单报文提交到消息队列完成");
        } catch (Exception e) {
            logger.error(getName()+"构建指令或将订单报文提交到消息队列失败: "+ e.getMessage(), e);
        }
    }

    /**
     * 执行自定义指令组合
     * @param operation 指令组合标识字符串
     */
    private void executeOperation(String operation) throws Exception {
        operation = requireNonNull(operation, "operation is null");
        if (!isEnabled()) {
            return;
        }
        IAction actionTemplate = AppContext.getActionTemplateMap().get(operation);
        if(ToolsKit.isEmpty(actionTemplate)) {
            actionTemplate = AppContext.getActionTemplateMap().get(operation.toUpperCase());
            if(ToolsKit.isEmpty(actionTemplate)) {
                actionTemplate = AppContext.getActionTemplateMap().get(operation.toLowerCase());
            }
        }
        if(ToolsKit.isEmpty(actionTemplate)) {
            throw new NullPointerException("请先配置需要执行的自定义指令组合，名称需要一致，不区分大小写");
        }
        logger.info(getName()+": 开始执行自定义指令集合["+operation+"]操作");
        actionTemplate.execute();

    }

    /**
     * 清理命令队列
     */
    @Override
    public synchronized void clearCommandQueue() {
        commandMap.clear();
        Queue<Map<String, TelegramQueueDto>> queue = AppContext.getTelegramQueue().get(getName());
        if(ToolsKit.isNotEmpty(queue)) {
            queue.clear();
        }
        super.clearCommandQueue();
        logger.info("###########clearCommandQueue");
    }

    /***
     * 链接车辆
     */
    @Override
    protected void connectVehicle() {
        logger.info("###########connectVehicle");
        // TODO 可以改为下拉选择的方式 ，待完成，目前先将起点位置设置为Point-0001
        getProcessModel().setVehiclePosition("36");
//        getProcessModel().setVehiclePosition("705");
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
            stateRequesterTask.disable();
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
     * 检查车辆位置并更新
     * @param response
     */
    public void checkForVehiclePositionUpdate(IResponse response) {

        // 将报告的位置ID映射到点名称
        String currentPosition = response.getNextPointName();
        // 更新位置，但前提是它不能是空
        if (ToolsKit.isNotEmpty(currentPosition)) {
            getProcessModel().setVehiclePosition(currentPosition);
        } else {
            logger.warn("车辆移动点不能为空，请确保response.getTargetPointName()返回的内容是下一移动点名称");
            return;
        }
        // Update GUI.
        synchronized (CommAdapter.this) {
            MovementCommand cmd = getSentQueue().poll();
            commandMap.remove(cmd);
            getProcessModel().commandExecuted(cmd);
            // 唤醒处于等待状态的线程
            CommAdapter.this.notify();
            logger.info("Vehicle["+getName()+"] move to "+ currentPosition+" point is success!");

            //到达最终停车点后判断是否有自定义操作，如果有匹配的标识符，则执行自定义操作
            if(!cmd.isWithoutOperation() && cmd.isFinalMovement()) {
                try {
                    executeOperation(cmd.getOperation());
                } catch (Exception e) {
                    logger.error("执行自定义指令时出错: " + e.getMessage(), e);
                }
            }
        }
    }


    /**
     * 必须实现，用于将值传递到控制中心的自定义面板
     * 启动时，面板点击更新后均会触发
     * @return

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
     */

}
