package com.robot.mvc.core.telegram;

import cn.hutool.http.HttpStatus;
import com.robot.RobotContext;
import com.robot.mvc.core.enums.ReqType;
import com.robot.mvc.core.exceptions.RobotException;
import com.robot.mvc.core.interfaces.IProtocol;
import com.robot.mvc.core.interfaces.IRequest;
import com.robot.mvc.core.interfaces.IResponse;

/**
 * Created by laotang on 2020/1/12.
 */
public class BaseResponse implements IResponse {

    /**
     * 响应对象ID，与请求ID一致
     */
    private String id;
    /**响应状态，值等于200时，为正常响应*/
    private int status;
    /**车辆或设备ID*/
    private String deviceId;
    /**响应指令，应与请求指令一致*/
    private String cmdKey;
    /**处理请求过程抛出的异常*/
    private Exception exception;
    /**握手验证码，在重发机制下，用于验证车辆或设备的应答回复，确保指令发送成功*/
    private String handshakeCode;
    /**
     * 协议原文字符串
     */
    protected String rawContent;

    public BaseResponse(IRequest request) {
        request = java.util.Objects.requireNonNull(request, "请求对象不能为空");
        IProtocol protocol = null;
        // 不是移动请求，则需要验证协议对象是否为null
        if (!ReqType.MOVE.equals(request.getReqType())) {
            protocol = java.util.Objects.requireNonNull(request.getProtocol(), "请求协议对象不能为空");
        }
        responseDefaultValue(request, protocol);
    }

    private void responseDefaultValue(IRequest request, IProtocol protocol) {
        this.id = request.getId();
        // 协议对象不为null
        if (null != protocol) {
            this.deviceId = protocol.getDeviceId();
            this.cmdKey = protocol.getCmdKey();
        }
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

    /**
     * 业务逻辑处理完成后，将返回对象写入到响应对象
     * @param message
     */
    @Override
    public void write(Object message) {
        if (message instanceof String) {
            setRawContent(String.valueOf(message));
        } else if (message instanceof IProtocol) {
            setRawContent(RobotContext.getRobotComponents().getProtocolMatcher().decode((IProtocol) message));
        } else {
            throw new RobotException("返回一个不支持的类型: " + message.getClass());
        }
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
