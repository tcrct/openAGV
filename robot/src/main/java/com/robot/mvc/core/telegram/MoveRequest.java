package com.robot.mvc.core.telegram;

import com.robot.RobotContext;
import com.robot.adapter.RobotCommAdapter;
import com.robot.mvc.core.enums.ReqType;
import com.robot.mvc.core.interfaces.IProtocol;
import com.robot.mvc.utils.SettingUtil;
import org.opentcs.drivers.vehicle.MovementCommand;

import java.util.List;

/**
 * 移动请求
 * 由opentcs adapter发起
 *
 * @author laotang
 * @blame Android Team
 * @since 2020/1/12
 */
public class MoveRequest extends BaseRequest {

    private List<MovementCommand> movementCommandList;

    public MoveRequest(IProtocol protocol) {
        super(ReqType.MOVE, protocol);
    }

    /**
     * 构造方法，将移动队列放置到移动请求对象中
     * @param adapter 车辆适配器
     * @param commandList 移动队列集合
     */
    public MoveRequest(RobotCommAdapter adapter, List<MovementCommand> commandList) {

        super(ReqType.MOVE, new IProtocol() {
            @Override
            public String getCmdKey() {
                return RobotContext.getRobotComponents().getMoveProtocolKey();
            }

            @Override
            public String getDeviceId() {
                return adapter.getName();
            }

            @Override
            public String getCode() {
                return null;
            }

            @Override
            public String getParams() {
                return null;
            }
        });
        super.adapter = adapter;
        this.movementCommandList = commandList;
    }

    /**
     * 取移动队列集合
     * @return
     */
    public List<MovementCommand> getMovementCommandList() {
        return movementCommandList;
    }
}
