package com.openagv.core.handshake;

import com.openagv.core.interfaces.ICallback;
import com.openagv.core.interfaces.IRequest;
import com.openagv.core.interfaces.IResponse;
import com.openagv.tools.ToolsKit;
import org.apache.log4j.Logger;

import java.io.Serializable;

/**
 * 存放在握手对队里的报文对象Dto
 *
 * @author Laotang
 */
public class HandshakeTelegramDto implements Serializable {

    private static final Logger logger = Logger.getLogger(HandshakeTelegramDto.class);

    /**请求对象*/
    private IRequest request;
    /**返回对象*/
    private IResponse response;
    /**注册回调事件*/
    private ICallback callback;
    /**指令动作列表名称*/
    private String actionKey;

    public HandshakeTelegramDto(HandshakeTelegramDto dto) {
        this(dto.getRequest(), dto.getResponse(),dto.getCallback(), dto.getActionKey());
    }

    public HandshakeTelegramDto(IResponse response) {
        this.response = response;
    }

    public HandshakeTelegramDto(IRequest request, IResponse response, ICallback callback, String actionKey) {
        this.request = request;
        this.response = response;
        this.callback = callback;
        this.actionKey = actionKey;
    }

    public IRequest getRequest() {
        return request;
    }

    public void setRequest(IRequest request) {
        this.request = request;
    }

    public IResponse getResponse() {
        return response;
    }

    public void setResponse(IResponse response) {
        this.response = response;
    }

    public ICallback getCallback() {
        return callback;
    }

    public void setCallback(ICallback callback) {
        this.callback = callback;
    }

    public String getActionKey() {
        return actionKey;
    }

    public void setActionKey(String actionKey) {
        this.actionKey = actionKey;
    }

    @Override
    public String toString() {
        if(ToolsKit.isEmpty(response)) {
            logger.error("HandshakeTelegramDto response is null");
            return "";
        }
        return "HandshakeTelegramDto{" +
                ", cmdKey=" + response.getCmdKey() +
                ", handshakeKey=" + response.getHandshakeKey() +
                ", telegram=" + response.toString() +
                ", callback=" + callback +
                '}';
    }
}
