package com.openagv.opentcs.telegrams;

import com.openagv.core.interfaces.ICallback;
import com.openagv.core.interfaces.IResponse;

import java.io.Serializable;

public class TelegramQueueDto implements Serializable {

    /**设备/车辆ID*/
    private String deviceId;
    /**握手验证码*/
    private String handshakeKey;
    /**请求ID*/
    private String reqeustId;
    /**注册回调事件*/
    private ICallback callback;
    /**返回结果*/
    private IResponse response;

    public TelegramQueueDto() {
    }

    public TelegramQueueDto(TelegramQueueDto dto) {
        this(dto.getDeviceId(),dto.getHandshakeKey(), dto.getReqeustId(), dto.getCallback(), dto.getResponse());
    }

    public TelegramQueueDto(String deviceId, String handshakeKey, IResponse response) {
        this.deviceId = deviceId;
        this.handshakeKey = handshakeKey;
        this.response = response;
    }

    public TelegramQueueDto(String deviceId, String handshakeKey, String requestId, ICallback callback, IResponse response) {
        this.deviceId = deviceId;
        this.handshakeKey = handshakeKey;
        this.reqeustId = requestId;
        this.callback = callback;
        this.response = response;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getHandshakeKey() {
        return handshakeKey;
    }

    public void setHandshakeKey(String handshakeKey) {
        this.handshakeKey = handshakeKey;
    }

    public ICallback getCallback() {
        return callback;
    }

    public void setCallback(ICallback callback) {
        this.callback = callback;
    }

    public IResponse getResponse() {
        return response;
    }

    public void setResponse(IResponse response) {
        this.response = response;
    }

    public String getReqeustId() {
        return reqeustId;
    }

    public void setReqeustId(String reqeustId) {
        this.reqeustId = reqeustId;
    }

    @Override
    public String toString() {
        return "TelegramQueueDto{" +
                "deviceId='" + deviceId + '\'' +
                ", handshakeKey='" + handshakeKey + '\'' +
                ", reqeustId='" + reqeustId + '\'' +
                ", callback=" + callback +
                ", response=" + response +
                '}';
    }
}
