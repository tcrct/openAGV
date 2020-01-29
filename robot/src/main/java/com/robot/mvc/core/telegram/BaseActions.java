package com.robot.mvc.core.telegram;

import com.robot.RobotContext;
import com.robot.adapter.RobotCommAdapter;
import com.robot.mvc.core.exceptions.RobotException;
import com.robot.mvc.core.interfaces.*;
import com.robot.mvc.utils.ActionsQueue;
import com.robot.mvc.utils.ToolsKit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * 工站指令集算法基类
 *
 * @author Laotang
 * @blame Android Team
 */
public abstract class BaseActions implements IAction {

    private static final Logger LOG = LoggerFactory.getLogger(BaseActions.class);

    /**
     * 动作请求队列对象
     */
    private ActionsQueue actionsQueue;

    private RobotCommAdapter adapter;
    /**
     * 发送对象
     */
    private ISender sender;
    /**
     * 请求重发对象
     */
    private IRepeatSend repeatSend;
    /**
     * 车辆是否已经提前移走
     */
    private boolean isVehicleMove = false;

    public BaseActions() {
        actionsQueue = ActionsQueue.duang();
        repeatSend = RobotContext.getRobotComponents().getRepeatSend();
        RobotContext.getRobotComponents().getProtocolDecode();
        // TODO 待实现
        sender = null;
    }

    @Override
    public void execute() throws Exception {
        //确认车辆没有提前移走
        isVehicleMove = false;

        // 工站动作名称
        String actionKey = actionKey();
        List<IActionCommand> requestList = new ArrayList<>();
        // 车辆ID
        String vehicleId = vehicleId();
        // 设备ID
        String deviceId = deviceId();
        // 根据子类里的add方法，添加动作指令
        add(requestList);
        // 添加到队列
        putQueue(actionKey, vehicleId, deviceId, requestList);
        // 发送第一条指令
        try {
            sendTelegram(actionKey);
        } catch (Exception e) {
            throw new RobotException("设备[" + deviceId() + "]执行[" + actionKey + "]工站任务时出错: " + e.getMessage(), e);
        }

    }

    private void putQueue(String actionKey, String vehicleId, String deviceId, List<IActionCommand> requestList) throws Exception {
        if (null == requestList || requestList.isEmpty()) {
            throw new NullPointerException("请求指令集不能为空");
        }

        Queue<IRequest> queue = actionsQueue.getQueue(actionKey);
        Double index = 1D; //从1开始，以便在1的位置前插入一个指令
        for (IActionCommand command : requestList) {
            ActionRequest request = null;
            if (command instanceof ActionRequest) {
                request = (ActionRequest) command;
            } else if (command instanceof ActionResponse) {
                ActionResponse response = (ActionResponse) command;
                request = response.toActionRequest();
            }
            if (ToolsKit.isEmpty(request)) {
                throw new NullPointerException("请求对象不能为空");
            }
            IProtocol protocol = Objects.requireNonNull(request.getProtocol(), "协议对象不能为空");
            deviceId = ToolsKit.isEmpty(protocol.getDeviceId()) ? deviceId : protocol.getDeviceId();

            Protocol protocolNew = new Protocol.Builder()
//                        .serialPortAddress(deviceAddress)
                    .deviceId(deviceAddress)
                    .direction(ToolsKit.isEmpty(protocol.getDirection()) ? RobotEnum.UP_LINK.getValue() : protocol.getDirection())
                    .commandKey(ToolsKit.isEmpty(protocol.getCommandKey()) ? request.cmd() : protocol.getCommandKey())
                    .params(protocol.getParams())
                    .build();
            protocolNew.setCode(CrcUtil.CrcVerify_Str(ProtocolUtils.builderCrcString(protocolNew)));
//                request(protocolNew.getFunctionCommand());
//                request.setOriginalTelegram(MakerwitUtil.buildProtocolString(protocolNew));
            request.setProtocol(protocolNew);
//            }
            // 设置索引，排序用，提供改变队列中元素的顺序的能力
            request.setIndex(index++);
            request.setVehicleId(vehicleId); //设备车辆ID
            queue.add(request);
        }
        actionsQueue.put(actionKey, queue);
        actionsQueue.getAllRequest();
    }

    private void sendTelegram(String actionKey) throws Exception {
        Queue<IRequest> queue = Objects.requireNonNull(actionsQueue.getQueue(actionKey), "根据" + actionKey + "查找指令队列不能为空");
        IRequest request = peekRequest(queue).isPresent() ? peekRequest(queue).get() : null;
        if (null == request) {
            LOG.info("指令集为空，退出");
            return;
        }
        IProtocol protocol = request.getProtocol();
        // 先判断是否为VehicleMoveRequest，如果是，则作特殊处理，移动车辆指令发送后，系统会执行下发路径指令到车辆
        if (ToolsKit.isNotEmpty(request) && ToolsKit.isNotEmpty(protocol)) {
            if (VehicleMoveRequest.CMD_FIELD.equals(protocol.getCommandKey())) {
                String protocolParams = protocol.getParams();
                if (VehicleMoveRequest.DEFAULT_PARAM.equals(protocolParams)) {
                    // 移除当前的指令，即移除当前第一个位置的移动车辆指令
                    if (remove(queue)) {
                        // 取出下一位置的指令继续执行，这里会执行车辆自有的重发机制
                        adapter.executeNextMoveCmd(protocol.getDeviceId(), actionKey);
                        isVehicleMove = true;
                        // 取出下一位
                        request = peekRequest(queue).isPresent() ? peekRequest(queue).get() : null;
                        LOG.info("移动车辆指令已发送，继续执行下一个指令请求");
                    }
                }
//                else {
//                    logger.info("执行指定的移动车辆指令: " + protocolParams);
//                    RequestCallbackFactory.callback(new SetrOutRequest(protocol.getDeviceId(), protocolParams));
//                    remove(queue);
//                    AppContext.getCommAdapter().executeNextMoveCmd(protocol.getDeviceId(), actionKey);
//                    isVehicleMove = true;
//                    // 取出下一位
//                    request = peekRequest(queue).isPresent() ? peekRequest(queue).get() : null;
//                }
            }
//            if (ToolsKit.isNotEmpty(request) && ServiceRequest.CMD_FIELD.equals(request.getCmdKey())) {
//                Protocol protocol = (Protocol) request.getProtocol();
//                String jsonText = protocol.getParams();
//                ServiceAction serviceAction = ToolsKit.jsonParseObject(jsonText, ServiceAction.class);
//                String serviceName = serviceAction.getServiceName();
//                if(ToolsKit.isEmpty(serviceName)) {
//                  throw new AgvException("逻辑处理服务名(全称)不能为空");
//                }
//                Iterator<Object> iterator = AppContext.getInjectClassObjectSet().iterator();
//                final List<Object> targetServiceObjList = new ArrayList<>();
//                while (iterator.hasNext()) {
//                    Object serviceObj = iterator.next();
//                    if (serviceName.equalsIgnoreCase(serviceObj.getClass().getName())) {
//                        targetServiceObjList.add(serviceObj);
//                        break;
//                    }
//                }
//                if(ToolsKit.isEmpty(targetServiceObjList)) {
//                    throw new AgvException("逻辑处理服务类不存在");
//                }
//                String methodName = serviceAction.getMethodName();
//                if(ToolsKit.isEmpty(methodName)) {
//                    throw new AgvException("逻辑处理服务类方法名不能为空");
//                }
//                final boolean[] isSuccess = {false};
//                try {
//                    // 开启线程等待
//                    final CountDownLatch latch = new CountDownLatch(1);
//                    ThreadUtil.execute(new Runnable() {
//                        @Override
//                        public void run() {
//                            boolean isOk = false;
//                            try {
//                                while (!isOk) {
//                                    // 每两秒调用一次指定的方法
//                                    ThreadUtil.safeSleep(2000L);
//                                    Object result = ReflectUtil.invoke(targetServiceObjList.get(0), methodName, serviceAction);
//                                    isOk = Boolean.parseBoolean(String.valueOf(result));
//                                    if (isOk) {
//                                        isSuccess[0] = true;
//                                        latch.countDown(); //线程结束
//                                    } else {
//                                        logger.info("当前通道有货物在等待，等待传感器状态，状态为0时放行");
//                                    }
//                                }
//                            } catch (Exception e) {
//                                logger.error("调用业务逻辑服务类时出错: " + e.getMessage(), e);
//                            }
//                        }
//                    });
//                    latch.await(); //线程等待结束
//                } catch (Exception e) {
//                    logger.info("该方法只允许返回布尔值");
//                }
//                // 如果返回true，则执行下一条命令
//                if (isSuccess[0]) {
//                    remove(queue);
//                    // 取出下一位
//                    request = peekRequest(queue).isPresent() ? peekRequest(queue).get() : null;
//                }
//            }
        }
        //退出指令集动作
        if (null == request) {
            ActionsQueue.duang().clearVerificationCodeMap(actionKey);
            LOG.info("指令集为空，退出");
            return;
        }
        // 模拟发送，先经过Service处理，得到response后，再加入到握手队列
        Response response = SendRequest.duang().send((ActionRequest) request, sender);
        if (AppContext.isHandshakeListener()) {
            ICallback<String> callback = new ICallback<String>() {
                @Override
                public void call(String actionKey, String requestId, String deviceId) throws Exception {
                    callback(actionKey, requestId, deviceId);
                }
            };
            handshakeTelegram.add(new HandshakeTelegramDto(
                    request, response, callback, actionKey));
        }
//        //如果BaseResponse或者是上行方向且是rpt开头的命令，则不需要发送报文
//        if(!(request instanceof BaseResponse) &&
//                (!"s".equalsIgnoreCase(response.getDirection()) &&
//                !response.getProtocol().getCommandKey().toLowerCase().startsWith("rpt"))) {
//            sender.sendTelegram(response);
//        }
    }

    private Optional<Request> peekRequest(Queue<Request> queue) {
        return Optional.ofNullable(queue.peek());
    }

    /***
     * 回调
     * @param actionKey 动作指令
     * @param requestId 请求ID
     * @param deviceId 车辆ID
     * @throws Exception
     */
    private void callback(String actionKey, String requestId, String deviceId) throws Exception {
        Queue<Request> queue = actionsQueue.getQueue(actionKey);
        if (peekRequest(queue).isPresent()) {
            Request request = peekRequest(queue).get();
            if (ToolsKit.isNotEmpty(request) &&
                    ToolsKit.isEmpty(deviceId) &&
                    (request instanceof ActionRequest)) {
                deviceId = ((ActionRequest) request).getVehicleId();
            }
            if (ToolsKit.isNotEmpty(request) && request.getId().equals(requestId)) {
                if (remove(queue)) {//移除第一位请求
                    sendTelegram(actionKey);// 发送下一个请求
                }
            }
        }
        // 如果队列为空，则移动车辆
        if (!peekRequest(queue).isPresent()) {
            //清空所有动作请求集合
            ActionsQueue.duang().clearVerificationCodeMap(actionKey);
            LOG.info("清空所有动作请求集合完成");
            executeMoveVehicleCmd(deviceId, actionKey);
        }
    }

    private void executeMoveVehicleCmd(String deviceId, String actionKey) {
        actionsQueue.remove(actionKey);
        if (!isVehicleMove) {
            adapter.executeNextMoveCmd(deviceId, actionKey);
        }
    }

    private boolean remove(Queue<Request> queue) {
        try {
            Request request = queue.peek();
            queue.remove();
            LOG.info("移除设备任务队列[ " + request.getRawContent() + " ]成功！");
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 注明该动作名称，要与openTCS里的位置类研->动作一致
     * 系统用于确定车辆到达指定位置后执行的动作指令
     *
     * @return 动作名称
     */
    @Override
    public abstract String actionKey();

//    /**
//     * 车辆或串口模块地址的名称，确定该指令集仅制于该车辆或串口
//     *
//     * @return 车辆/串口模块名称
//     */
//    public abstract String deviceId();

    /**
     * 添加该指令集的请求动作，动作顺序以存放顺序一致，即在位置靠前的先执行。
     *
     * @param requestList 要执行的请求指令的有序数组
     */
    public abstract void add(List<IActionCommand> requestList);

}
