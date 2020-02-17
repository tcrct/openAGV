package com.robot.mvc.core.telegram;

import com.robot.adapter.RobotCommAdapter;
import com.robot.mvc.core.enums.ReqType;
import com.robot.mvc.core.interfaces.IProtocol;
import com.robot.mvc.utils.RobotUtil;
import org.opentcs.drivers.vehicle.MovementCommand;

import java.util.Queue;

/**
 * 移动请求
 * 由opentcs adapter发起
 *
 * @author laotang
 * @blame Android Team
 * @since 2020/1/12
 */
public class MoveRequest extends BaseRequest {

    private Queue<MovementCommand> movementCommandQueue;

    public MoveRequest(IProtocol protocol) {
        super(ReqType.MOVE, protocol);
    }

    /**
     * 构造方法，将移动队列放置到移动请求对象中
     *
     * @param adapter     车辆适配器
     * @param commandList 移动队列集合
     */
    public MoveRequest(RobotCommAdapter adapter, Queue<MovementCommand> commandList) {
        super(ReqType.MOVE, null);
        super.protocol = new MoveProtocol(adapter.getName(), RobotUtil.getMoveProtocolKey());
        super.adapter = adapter;
        this.movementCommandQueue = commandList;
        super.setNeedSend(true);
    }

    /**
     * 取移动队列集合
     *
     * @return
     */
    public Queue<MovementCommand> getMovementCommandQueue() {
        return movementCommandQueue;
    }

    //定义一个内部类
    class MoveProtocol implements IProtocol {
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

        MoveProtocol(String deviceId, String cmdKey) {
            this.deviceId = deviceId;
            this.cmdKey = cmdKey;
        }

        public MoveProtocol(String deviceId, String cmdKey, String code, String params) {
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


