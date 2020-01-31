package com.makerwit.core.component;

import com.makerwit.utils.ProtocolUtil;
import com.robot.RobotContext;
import com.robot.adapter.RobotCommAdapter;
import com.robot.mvc.core.exceptions.RobotException;
import com.robot.mvc.core.interfaces.*;
import com.robot.mvc.core.telegram.ActionRequest;
import com.robot.mvc.core.telegram.ActionResponse;
import com.robot.mvc.core.telegram.BaseResponse;
import com.robot.mvc.main.DispatchFactory;
import com.makerwit.model.ActionCallback;
import com.robot.mvc.utils.ActionsQueue;
import com.robot.mvc.utils.ToolsKit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

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
     * 动作指令Map, key为握手工站命称KEY
     * 将指令发送到工站设备后，收到动作完成的回复后，在ActionCallbackHandler处理
     */
    private static final Map<String, Queue<ActionCallback>> ACTION_CALLBACK_MAP = new ConcurrentHashMap<>();
    /**
     * 车辆是否已经提前移走
     */
    private boolean isVehicleMove = false;

    public BaseActions() {
        actionsQueue = ActionsQueue.duang();
        repeatSend = RobotContext.getRobotComponents().getRepeatSend();
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
            Protocol protocol = Objects.requireNonNull((Protocol) request.getProtocol(), "协议对象不能为空");
            request.setProtocol(protocol);
            // 设置索引，排序用，提供改变队列中元素的顺序的能力
            request.setIndex(index++);
            //设备车辆ID
            request.setVehicleId(vehicleId);
            queue.add(request);
            addActionRequest2Map(protocol, request.getId(), actionKey, vehicleId);
        }
        actionsQueue.put(actionKey, queue);
//        actionsQueue.getAllRequest();
    }

    private void sendTelegram(String actionKey) throws RobotException {
        Queue<IRequest> queue = Objects.requireNonNull(actionsQueue.getQueue(actionKey), "根据" + actionKey + "查找指令队列不能为空");
        IRequest request = peekRequest(queue).isPresent() ? peekRequest(queue).get() : null;
        if (null == request) {
            LOG.info("工站[{}]指令集为空，退出发送!", actionKey);
            return;
        }
        if (!(request instanceof ActionRequest)) {
            LOG.info("工站[{}]指令集的请求不是ActionRequest，退出发送!", actionKey);
            return;
        }
        ActionRequest actionRequest = (ActionRequest) request;
        String vehicleId = actionRequest.getVehicleId();
        Protocol protocol = (Protocol) request.getProtocol();
        if (ToolsKit.isEmpty(protocol)) {
            LOG.info("工站[{}]指令集取出的协议对象为空，退出发送!", actionKey);
            return;
        }
        // 先判断是否为VehicleMoveRequest，如果是，则作特殊处理，移动车辆指令发送后，系统会执行下发路径指令到车辆
//        if (ToolsKit.isEmpty(actionRequest) && ToolsKit.isNotEmpty(protocol)) {
//            if (VehicleMoveRequest.CMD_FIELD.equals(protocol.getCommandKey())) {
//                String protocolParams = protocol.getParams();
//                if (VehicleMoveRequest.DEFAULT_PARAM.equals(protocolParams)) {
//                    // 移除当前的指令，即移除当前第一个位置的移动车辆指令
//                    if (remove(queue)) {
//                        // 取出下一位置的指令继续执行，这里会执行车辆自有的重发机制
//                        adapter.executeNextMoveCmd(protocol.getDeviceId(), actionKey);
//                        isVehicleMove = true;
//                        // 取出下一位
//                        request = peekRequest(queue).isPresent() ? peekRequest(queue).get() : null;
//                        LOG.info("移动车辆指令已发送，继续执行下一个指令请求");
//                    }
//                }
//            }
        // 模拟发送，先经过Service处理，得到response后，再加入到握手队列，以便在Service里添加业务逻辑
        DispatchFactory.dispatch(actionRequest, new BaseResponse(actionRequest));
    }

    private Optional<IRequest> peekRequest(Queue<IRequest> queue) {
        return Optional.ofNullable(queue.peek());
    }

    /***
     * 回调方法，执行指令队列里的下一条指令
     * @param actionKey 动作指令
     * @param requestId 请求ID
     * @param vehicleId 车辆ID
     * @throws Exception
     */
    private void callback(String actionKey, String requestId, String code, String vehicleId) throws RobotException {
        Queue<IRequest> queue = actionsQueue.getQueue(actionKey);
        if (peekRequest(queue).isPresent()) {
            IRequest request = peekRequest(queue).get();
            if (ToolsKit.isNotEmpty(request) && request.getId().equals(requestId)) {
                //移除第一位请求
                if (remove(queue)) {
                    // 根据code移除Map里对应的缓存
                    Queue<ActionCallback> actionCallbackQueue = ACTION_CALLBACK_MAP.get(actionKey);
                    if (ToolsKit.isNotEmpty(actionCallbackQueue)) {
                        ActionCallback actionCallback = actionCallbackQueue.peek();
                        if (ToolsKit.isNotEmpty(actionCallback)) {
                            if (actionCallback.getCode().equals(code) && actionCallback.getId().equals(requestId)) {
                                actionCallbackQueue.remove();
                            } else {
                                throw new RobotException("移除ActionCallbackMap里指定队列[" + actionKey + "]里的元素[" + code + "]失败!");
                            }
                        }
                    }
                    sendTelegram(actionKey);// 发送下一个请求
                }
            }
        }// 如果队列为空，则移动车辆
        else {
            executeMoveVehicleCmd(vehicleId, actionKey);
        }
    }

    /**
     * 执行车辆移动命令
     *
     * @param vehicleId
     * @param actionKey
     */
    private void executeMoveVehicleCmd(String vehicleId, String actionKey) {
        // 移除指定的队列集
        actionsQueue.remove(actionKey);
        if (!isVehicleMove) {
//            adapter.executeNextMoveCmd(vehicleId, actionKey);
        }
    }

    /**
     * 删除队列里的第1位元素的指令
     *
     * @param queue
     * @return
     */
    private boolean remove(Queue<IRequest> queue) {
        try {
            IRequest request = queue.peek();
            queue.remove();
            LOG.info("移除工站指令集任务队列[{}]成功!", request.getRawContent());
            return true;
        } catch (Exception e) {
            LOG.info("移除设备任务队列失败: {}", e.getMessage(), e);
            return false;
        }
    }

    public static Map<String, Queue<ActionCallback>> getActionCallbackMap() {
        return ACTION_CALLBACK_MAP;
    }

    /**
     * 添加动作指令到集合
     *
     * @param protocol  协议对象
     * @param requestId 请求ID
     * @param actionKey 工站名称
     * @param vehicleId 车辆ID
     */
    private void addActionRequest2Map(Protocol protocol, String requestId, String actionKey, String vehicleId) {
        IActionCallback callback = new IActionCallback() {
            @Override
            public void call(String actionKey, String requestId, String code, String vehicleId) throws RobotException {
                callback(actionKey, requestId, code, vehicleId);
            }
        };
        //设置握手验证码，将响应方式互换，即s的更改为r，r更改为s。再重新计算验证码
        //该验证码用于上报响应后，根据响应协议内容里的验证码，查找匹配
//        actionResponse.setHandshakeCode(ProtocolUtil.builderHandshakeCode(protocol));
        String handshakeCode = ProtocolUtil.builderHandshakeCode(protocol);
        ActionCallback actionCallback = new ActionCallback(requestId,
                vehicleId,
                handshakeCode,
                actionKey,
                callback);
        Queue actionCallbackQueue = ACTION_CALLBACK_MAP.get(actionKey);
        if (ToolsKit.isEmpty(actionCallbackQueue)) {
            actionCallbackQueue = new LinkedList();
        }
        actionCallbackQueue.add(actionCallback);
        //添加到Map
        ACTION_CALLBACK_MAP.put(actionKey, actionCallbackQueue);
    }

    /**
     * 注明该动作名称，要与openTCS里的位置类研->动作一致
     * 系统用于确定车辆到达指定位置后执行的动作指令
     *
     * @return 动作名称
     */
    @Override
    public abstract String actionKey();

    /**
     * 添加该指令集的请求动作，动作顺序以存放顺序一致，即在位置靠前的先执行。
     *
     * @param requestList 要执行的请求指令的有序数组
     */
    public abstract void add(List<IActionCommand> requestList);

}
