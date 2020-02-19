package com.robot.mvc.core.telegram;

import cn.hutool.http.HttpStatus;
import com.robot.RobotContext;
import com.robot.adapter.model.RobotStateModel;
import com.robot.mvc.core.exceptions.RobotException;
import com.robot.mvc.core.interfaces.IProtocol;
import com.robot.mvc.core.interfaces.IRequest;
import com.robot.mvc.core.interfaces.IResponse;
import com.robot.utils.ToolsKit;

/**
 * 响应对象基类
 *
 * @author Laotang
 * @date 2020/1/12
 */
public class BaseResponse implements IResponse {

    /**
     * 响应对象ID，与请求ID一致
     */
    private String id;
    /**
     * 响应状态，值等于200时，为正常响应
     */
    private int status;
    /**
     * 车辆或设备ID
     */
    private String deviceId;
    /**
     * 响应指令，应与请求指令一致
     */
    private String cmdKey;
    /**
     * 处理请求过程抛出的异常
     */
    private Exception exception;
    /**
     * 握手验证码，在重发机制下，用于验证车辆或设备的应答回复，确保指令发送成功
     */
    private String handshakeCode;
    /**
     * 是否需要适配器操作，默认为fase, 值为true时需要适配器后续操作
     */
    private boolean isNeedAdapterOperation;
    /**
     * 是否需要发送协议，默认为fase, 值为true时需要发送
     */
    private boolean isNeedSend;
    /*** 协议字符串*/
    protected String rawContent;

    private RobotStateModel robotStateModel;

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
        if (request.isNeedAdapterOperation()) {
            setNeedAdapterOperation(true);
        }
        if (request.isNeedSend()) {
            setNeedSend(true);
        }
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setStatus(int status) {
        this.status = status;
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
     *
     * @param message
     */
    @Override
    public void write(Object message) {

        if (ToolsKit.isEmpty(message)) {
            throw new RobotException("写入到响应对象时，值对象不能为空！");
        }

        if (message instanceof String) {
            setRawContent(String.valueOf(message));
        } else if (message instanceof IProtocol) {
            setRawContent(RobotContext.getRobotComponents().getProtocolMatcher().decode((IProtocol) message));
        } else {
            throw new RobotException("返回一个不支持的类型: " + message.getClass());
        }
    }

    /**
     * 设置异常信息
     */
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
    public boolean isNeedAdapterOperation() {
        return isNeedAdapterOperation;
    }

    /***
     * 设置是否需要适配器继续操作，默认为false, 为true时代表需要
     * @param needAdapterOperation  是否需要
     */
    public void setNeedAdapterOperation(boolean needAdapterOperation) {
        isNeedAdapterOperation = needAdapterOperation;
    }

    @Override
    public boolean isNeedSend() {
        return isNeedSend;
    }

    @Override
    public RobotStateModel getRobotStateModel() {
        if (null == robotStateModel) {
            throw new RobotException("请在Service里实现RobotStateModel设置，" +
                    "该model不能为null，用于Adapter根据该model更新位置及是否进行工站操作等处理！");
        }
        return robotStateModel;
    }

    /**
     * 设置Robot adapter需要的状态模型对象
     *
     * @param robotStateModel 状态模型对象
     */
    public void setRobotStateModel(RobotStateModel robotStateModel) {
        this.robotStateModel = robotStateModel;
    }

    /**
     * 设置是否需要发送协议，默认为false, 为true时代表需要发送
     *
     * @param needSend
     */
    public void setNeedSend(boolean needSend) {
        isNeedSend = needSend;
    }

    @Override
    public String getRawContent() {
        return rawContent;
    }

    @Override
    public void setRawContent(String raw) {
        rawContent = raw;
    }

    @Override
    public String toString() {
        return rawContent;
    }
}
