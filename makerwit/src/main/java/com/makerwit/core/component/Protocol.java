package com.makerwit.core.component;

import com.makerwit.utils.ProtocolUtil;
import com.robot.mvc.core.interfaces.IProtocol;
import com.robot.mvc.utils.CrcUtil;
import com.robot.mvc.utils.ToolsKit;

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

        /**
         * 设备ID标识符
         *
         * @param deviceId 设备ID标识符
         * @return
         */
        public Builder deviceId(String deviceId) {
            this.deviceId = java.util.Objects.requireNonNull(deviceId, "deviceId is null");
            return this;
        }

        /***
         * 协议对象发送方向
         * @param direction 协议对象发送方向
         * @return
         */
        public Builder direction(String direction) {
            this.direction = java.util.Objects.requireNonNull(direction, "direction is null");
            return this;
        }

        /**
         * 操作指令
         * @param cmdKey 操作指令
         * @return
         */
        public Builder cmdKey(String cmdKey) {
            this.cmdKey = java.util.Objects.requireNonNull(cmdKey, "cmdKey is null");
            return this;
        }

        /**
         * 参数字符串
         * @param params 参数字符串
         * @return
         */
        public Builder params(String params) {
            this.params = java.util.Objects.requireNonNull(params, "params is null");;
            return this;
        }

        /**
         * 协议对象字符串内容的验证码
         * @param code 验证码
         * @return
         */
        public Builder code(String code) {
            this.code = java.util.Objects.requireNonNull(code, "crc is null");;
            return this;
        }

        /**
         * 构建协议对象
         * @return Protocol对象
         */
        public Protocol build() {
            Protocol protocol = new Protocol(deviceId, direction, cmdKey, params, code);
            if (ToolsKit.isEmpty(code)) {
                protocol.setCode(CrcUtil.CrcVerify_Str(ProtocolUtil.builderCrcString(protocol)));
            }
            return protocol;
        }
    }

}
