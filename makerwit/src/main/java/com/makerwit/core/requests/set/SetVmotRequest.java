package com.makerwit.core.requests.set;

import com.makerwit.core.component.Protocol;
import com.makerwit.numes.CmdKeyEnum;
import com.makerwit.numes.MakerwitEnum;
import com.openagv.mvc.core.telegram.ActionRequest;

/**
 * 设置动作请求
 *
 * @author Laotang
 */
public class SetVmotRequest extends ActionRequest {

    public SetVmotRequest(String deviceId, String param) {
        super(new Protocol.Builder()
                .deviceId(deviceId)
                .direction(MakerwitEnum.UP_LINK.getValue())
                .cmdKey(CmdKeyEnum.SETVMOT.getValue())
                .params(param)
                .build());
    }

    @Override
    public String cmd() {
        return CmdKeyEnum.SETVMOT.getValue();
    }
}
