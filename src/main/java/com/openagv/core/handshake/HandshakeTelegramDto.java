package com.openagv.core.handshake;

import com.openagv.core.interfaces.ICallback;
import com.openagv.core.interfaces.IRequest;
import com.openagv.core.interfaces.IResponse;
import com.openagv.opentcs.telegrams.OrderRequest;
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

    public HandshakeTelegramDto(HandshakeTelegramDto dto) {
        this(dto.getRequest(), dto.getResponse(),dto.getCallback());
    }

    public HandshakeTelegramDto(IResponse response) {
        this.response = response;
    }

    public HandshakeTelegramDto(IRequest request, IResponse response, ICallback callback) {
        this.request = request;
        this.response = response;
        this.callback = callback;
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

    @Override
    public String toString() {
        if(ToolsKit.isEmpty(response)) {
            logger.error("HandshakeTelegramDto response is null");
            return "";
        }
        return "HandshakeTelegramDto{" +
                "isServerSend=" + response.isServerSend() +
                ", cmdKey=" + response.getCmdKey() +
                ", handshakeKey=" + response.getHandshakeKey() +
                ", telegram=" + response.toString() +
                ", callback=" + callback +
                '}';
    }
}
