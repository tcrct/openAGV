package com.robot.mvc.core.telegram;

import com.robot.mvc.core.enums.ReqType;
import com.robot.mvc.core.interfaces.IProtocol;
import com.robot.mvc.core.interfaces.IRequest;

/**
 * 工站动作响应对象
 * 一般用于等待响应对象回复，以确认操作。
 * 例如等待传感器数据回复
 *
 * Created by laotang on 2020/1/12.
 */
public abstract class ActionResponse {


    protected IProtocol protocol;

    public ActionResponse(IProtocol protocol) {
        this.protocol = protocol;
    }

    /***
     * 将响应对象转换为请求对象
     * 模拟客户端提交的对象数据，存放在队列中等待响应后删除
     * @return
     */
    public IRequest toRequest() {
        return new BaseRequest(ReqType.ACTION, protocol);
    }

    public abstract String cmd();
}
