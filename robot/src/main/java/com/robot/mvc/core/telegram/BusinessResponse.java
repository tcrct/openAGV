package com.robot.mvc.core.telegram;

import com.robot.mvc.core.interfaces.IRequest;

/**
 * 业务请求的响应对象
 * 一般用于等待响应对象回复，以确认操作。
 * <p>
 * Created by laotang on 2020/1/28.
 */
public class BusinessResponse extends BaseResponse {

    public BusinessResponse(IRequest request) {
        super(request);
    }
}
