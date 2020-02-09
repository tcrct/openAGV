package com.robot.mvc.core.telegram;

import com.robot.mvc.core.interfaces.IActionCommand;
import com.robot.mvc.core.interfaces.IProtocol;

/**
 * 工站动作响应对象
 * 一般用于等待响应对象回复，以确认操作。
 * 例如等待传感器数据回复
 *
 * Created by laotang on 2020/1/12.
 */
public abstract class ActionResponse implements IActionCommand {


    protected IProtocol protocol;

    public ActionResponse(IProtocol protocol) {
        this.protocol = protocol;
    }

    /***
     * 将响应对象转换为请求对象
     * 模拟客户端提交的对象数据，存放在队列中等待响应后删除
     * @return
     */
    public ActionRequest toActionRequest() {
        ActionRequest actionRequest = new ActionRequest(protocol) {
            @Override
            public String cmd() {
                return protocol.getCmdKey();
            }
        };
        // 等待回复的响应，一律不需要发送
        actionRequest.setNeedSend(false);
        return actionRequest;
    }

}
