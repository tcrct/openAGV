package com.makerwit.core.requests.get;

import com.makerwit.core.component.Protocol;
import com.makerwit.numes.CmdKeyEnum;
import com.makerwit.numes.MakerwitEnum;
import com.robot.mvc.core.telegram.ActionRequest;

/**
 * 查下磁导传感器导通状态
 *
 * @author Laotang
 */
public class GetMagRequest extends ActionRequest {

    public GetMagRequest(String deviceId, String params) {
        super(new Protocol.Builder()
                .deviceId(deviceId)
                .direction(MakerwitEnum.UP_LINK.getValue())
                .cmdKey(CmdKeyEnum.GETMAG.getValue())
                .params(params)
                .build());
    }

    @Override
    public String cmd() {
        return CmdKeyEnum.GETMAG.getValue();
    }
}
