package com.makerwit.core.requests.set;

import com.makerwit.core.component.Protocol;
import com.makerwit.numes.CmdKeyEnum;
import com.makerwit.numes.MakerwitEnum;
import com.robot.mvc.core.telegram.ActionRequest;

/**
 * 停车
 *
 * @author Laotang
 */
public class SetStpRequest extends ActionRequest {

    public SetStpRequest(String deviceId, String param) {
        super(new Protocol.Builder()
                .deviceId(deviceId)
                .direction(MakerwitEnum.UP_LINK.getValue())
                .cmdKey(CmdKeyEnum.SETSTP.getValue())
                .params(param)
                .build());
    }

    @Override
    public String cmd() {
        return CmdKeyEnum.SETSTP.getValue();
    }

}
