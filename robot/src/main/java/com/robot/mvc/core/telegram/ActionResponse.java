package com.robot.mvc.core.telegram;

import com.robot.mvc.core.interfaces.IRequest;

/**
 * 工站动作响应对象
 * 一般用于等待响应对象回复，以确认操作。
 * 例如等待传感器数据回复
 *
 * Created by laotang on 2020/1/12.
 */
public class ActionResponse extends BaseResponse {


    public ActionResponse(IRequest request) {
        super(request);
    }
}
