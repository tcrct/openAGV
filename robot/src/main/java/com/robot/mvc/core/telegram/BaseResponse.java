package com.robot.mvc.core.telegram;

import cn.hutool.http.HttpStatus;
import com.robot.mvc.core.interfaces.IProtocol;
import com.robot.mvc.core.interfaces.IRequest;
import com.robot.mvc.core.interfaces.IResponse;

/**
 * Created by laotang on 2020/1/12.
 */
public class BaseResponse implements IResponse {

    private String id;
    private int status;
    private String deviceId;
    private String cmdKey;
    private Exception exception;
    private String handshakeCode;
    /**
     * 协议原文字符串
     */
    protected String rawContent;

    public BaseResponse(IRequest request) {
        request = java.util.Objects.requireNonNull(request, "请求对象不能为空");
        IProtocol protocol = java.util.Objects.requireNonNull(request.getProtocol(), "请求协议对象不能为空");
        responseDefaultValue(request, protocol);
    }

    private void responseDefaultValue(IRequest request, IProtocol protocol) {
        this.id = request.getId();
        this.deviceId = protocol.getDeviceId();
        this.cmdKey = protocol.getCmdKey();
        this.exception = null;
        setStatus(HttpStatus.HTTP_OK);
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setStatus(int status) {
        this.status =status;
    }

    @Override
    public int getStatus() {
        return status;
    }

    @Override
    public String getDeviceId() {
        return deviceId;
    }

    @Override
    public String getCmdKey() {
        return cmdKey;
    }

    @Override
    public void write(Object message) {

    }

    /**设置异常信息*/
    public void setException(Exception exception) {
        this.exception = exception;
    }
    @Override
    public Exception getException() {
        return exception;
    }

    /***
     * 设置握手验证码code
     * 在重发机制下，用于比较请求与响应是否为同一操作过程
     *
     * @param code  握手验证码
     */
    public void setHandshakeCode(String code) {
        this.handshakeCode = code;
    }
    @Override
    public String getHandshakeCode() {
        return handshakeCode;
    }

    @Override
    public String getRawContent() {
        return rawContent;
    }

    @Override
    public void setRawContent(String raw) {
        rawContent = raw;
    }
}
