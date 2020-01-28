package com.makerwit.core.requests.set;

import com.makerwit.core.component.Protocol;
import com.makerwit.numes.CmdKeyEnum;
import com.makerwit.numes.MakerwitEnum;
import com.robot.mvc.core.telegram.ActionRequest;

/**
 * 下发路径指令
 *
 * @author Laotang
 */
public class SetrOutRequest extends ActionRequest {

    public SetrOutRequest(String deviceId, String param) {
        super(new Protocol.Builder()
                .deviceId(deviceId)
                .direction(MakerwitEnum.UP_LINK.getValue())
                .cmdKey(CmdKeyEnum.SETOUT.getValue())
                .params(param)
                .build());
    }

    @Override
    public String cmd() {
        return CmdKeyEnum.SETOUT.getValue();
    }
}
