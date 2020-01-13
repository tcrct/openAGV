package com.makerwit.core.protocol;

import com.openagv.mvc.core.interfaces.IProtocol;

/**
 * Created by laotang on 2020/1/13.
 */
public class Protocol implements IProtocol, java.io.Serializable {

    public static final String DEVICEID_FIELD = "deviceId";
    public static final String COMMAND_KEY_FIELD = "cmdKey";
    public static final String PARAMS_FIELD = "params";
    public static final String DIRECTION_FIELD = "direction";
    public static final String CRC_FIELD = "crc";

    /**设备/车辆 ID*/
    private String deviceId;
    /**功能指令*/
    private String cmdKey;
    /**参数*/
    private String params;
    /**方向,上下行*/
    private String direction;
    /**CRC验证码*/
    private String code;

    private Protocol(String deviceId, String direction, String cmdKey, String params, String code) {
        this.deviceId = deviceId;
        this.direction = direction;
        this.cmdKey = cmdKey;
        this.params = params;
        this.code = code;
    }

    @Override
    public String getCmdKey() {
        return null;
    }

    @Override
    public String getDeviceId() {
        return deviceId;
    }

    public String getParams() {
        return params;
    }

    public String getDirection() {
        return direction;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public static class Builder {
        private String deviceId;
        private String direction;
        private String cmdKey;
        private String params;
        private String code;

        public Builder deviceId(String deviceId) {
            this.deviceId = java.util.Objects.requireNonNull(deviceId, "deviceId is null");
            return this;
        }

        public Builder direction(String direction) {
            this.direction = java.util.Objects.requireNonNull(direction, "direction is null");
            return this;
        }

        public Builder cmdKey(String cmdKey) {
            this.cmdKey = java.util.Objects.requireNonNull(cmdKey, "cmdKey is null");
            return this;
        }

        public Builder params(String params) {
            this.params = java.util.Objects.requireNonNull(params, "params is null");;
            return this;
        }

        public Builder code(String code) {
            this.code = java.util.Objects.requireNonNull(code, "crc is null");;
            return this;
        }

        public Protocol build() {
            return new Protocol(deviceId, direction, cmdKey, params, code);
        }
    }

}
