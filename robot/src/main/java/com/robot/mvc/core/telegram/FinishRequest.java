package com.robot.mvc.core.telegram;

import com.robot.adapter.RobotCommAdapter;
import com.robot.mvc.core.enums.ReqType;
import com.robot.mvc.core.interfaces.IProtocol;
import org.opentcs.drivers.vehicle.MovementCommand;

import java.util.Queue;

/**
 * 订单完成请求
 * 由opentcs adapter发起
 *
 * @author laotang
 * @blame Android Team
 * @since 2020/1/12
 */
public class FinishRequest extends BaseRequest {

    private Queue<MovementCommand> movementCommandQueue;

    public FinishRequest(IProtocol protocol) {
        super(ReqType.FINISH, protocol);
    }

    /**该请求是否完成，用于标识移动订单完成时，通知业务系统*/
    private boolean isFinished;

    /**
     * 构造方法，将移动队列放置到移动请求对象中
     *
     * @param adapter     车辆适配器
     */
    public FinishRequest(RobotCommAdapter adapter) {
        super(ReqType.FINISH, null);
        super.protocol = new FinishProtocol(adapter.getName(), "finish", "0", "0");
        super.adapter = adapter;
        this.isFinished = true;
        // 不需要回到适配器进行操作
        super.setNeedAdapterOperation(false);
        //不需要发送到客户端
        super.setNeedSend(false);
    }

    public boolean isFinished() {
        return isFinished;
    }

    //定义一个内部类
    class FinishProtocol implements IProtocol {
        /**
         * 车辆/设备ID
         */
        private String deviceId;
        /**
         * 操作指令
         */
        private String cmdKey;
        /**
         * 验证码
         */
        private String code;
        /**
         * 参数
         */
        private String params;

        FinishProtocol(String deviceId, String cmdKey) {
            this.deviceId = deviceId;
            this.cmdKey = cmdKey;
        }

        public FinishProtocol(String deviceId, String cmdKey, String code, String params) {
            this.deviceId = deviceId;
            this.cmdKey = cmdKey;
            this.code = code;
            this.params = params;
        }

        @Override
        public String getCmdKey() {
            return cmdKey;
        }

        @Override
        public String getDeviceId() {
            return deviceId;
        }

        @Override
        public String getCode() {
            return code;
        }

        @Override
        public String getParams() {
            return params;
        }
    }

}


