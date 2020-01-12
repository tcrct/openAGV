package com.openagv.mvc.core.telegram;

import com.openagv.mvc.core.enums.ReqType;
import com.openagv.mvc.core.interfaces.IProtocol;
import org.opentcs.drivers.vehicle.MovementCommand;

import java.util.List;

/**
 * 移动请求
 * 由opentcs adapter发起
 *
 * Created by laotang on 2020/1/12.
 */
public class MoveRequest extends BaseRequest {

    private List<MovementCommand> commandList;

    public MoveRequest(IProtocol protocol) {
        super(ReqType.MOVE, protocol);
    }


}
