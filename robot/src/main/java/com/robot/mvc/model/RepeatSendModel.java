package com.robot.mvc.model;

import com.robot.mvc.core.interfaces.IActionCallback;
import com.robot.mvc.core.interfaces.IRequest;
import com.robot.mvc.core.interfaces.IResponse;

/**
 * 重发对象
 * Created by laotang on 2020/1/30.
 */
public class RepeatSendModel implements java.io.Serializable {

    /**
     * 请求
     */
    private IRequest request;
    /**
     * 响应
     */
    private IResponse response;
    /**
     * 回调事件
     */
    private IActionCallback callback;

    public RepeatSendModel(IRequest request, IResponse response, IActionCallback callback) {
        this.request = request;
        this.response = response;
        this.callback = callback;
    }

    public RepeatSendModel(IRequest request, IResponse response) {
        this.request = request;
        this.response = response;
    }

    public IRequest getRequest() {
        return request;
    }

    public IResponse getResponse() {
        return response;
    }

    public IActionCallback getCallback() {
        return callback;
    }
}
