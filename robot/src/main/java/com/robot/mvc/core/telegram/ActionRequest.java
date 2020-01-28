package com.robot.mvc.core.telegram;

import com.robot.mvc.core.enums.ReqType;
import com.robot.mvc.core.interfaces.IAction;
import com.robot.mvc.core.interfaces.IProtocol;

/**
 * 工作站动作请求
 * 当车辆到达指定位置后，对工作站发起的一连串动作指令请求
 * 包括发送请求，等待响应回复
 *
 * Created by laotang on 2020/1/12.
 */
public abstract class ActionRequest extends BaseRequest implements IAction {

    public ActionRequest(IProtocol protocol) {
        super(ReqType.ACTION, protocol);
    }

    public abstract String cmd();

}
