package com.makerwit.handlers;

import com.makerwit.core.component.BaseActions;
import com.makerwit.core.component.Protocol;
import com.makerwit.model.ActionCallback;
import com.makerwit.utils.MakerwitUtil;
import com.robot.mvc.core.exceptions.RobotException;
import com.robot.mvc.core.interfaces.*;
import com.robot.mvc.utils.ToolsKit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Queue;

/**
 * 根据返回的车辆标识符及验证码删除重发队列里的元素
 *
 * @author Laotang
 */
public class ActionCallbackHandler implements IHandler {

    private static final Logger LOG = LoggerFactory.getLogger(ActionCallbackHandler.class);

    @Override
    public boolean doHandler(String target, IRequest request, IResponse response) throws RobotException {
        Protocol protocol = (Protocol) request.getProtocol();
        String code = protocol.getCode();
        if (ToolsKit.isEmpty(code)) {
            throw new RobotException("验证码不能为空");
        }
        String actionKey = MakerwitUtil.getActionKey(protocol.getDeviceId());
        if (ToolsKit.isEmpty(actionKey)) {
            return true;
        }
        Queue<ActionCallback> actionCallbackQueue = BaseActions.getActionCallbackMap().get(actionKey);
        // 如果不存在则跳过
        if (ToolsKit.isEmpty(actionCallbackQueue)) {
            return true;
        }
        ActionCallback actionCallback = actionCallbackQueue.peek();
        if (ToolsKit.isEmpty(actionCallback)) {
            return true;
        }
        String handshakeCode = actionCallback.getCode();
        // 如果握手验证码一致
        if (code.equals(handshakeCode)) {
            IActionCallback callback = actionCallback.getCallback();
            if (ToolsKit.isNotEmpty(callback)) {
                // 回调执行下一个工站动作指令
                callback.call(actionCallback.getActionKey(), actionCallback.getId(), actionCallback.getCode(), actionCallback.getVehicleId());
                // 流程中止
                return false;
            }
        }
        return true;
    }
}
