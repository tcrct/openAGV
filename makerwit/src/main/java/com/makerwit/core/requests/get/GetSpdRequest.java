package com.makerwit.core.requests.get;

import com.makerwit.core.component.Protocol;
import com.makerwit.numes.CmdKeyEnum;
import com.makerwit.numes.MakerwitEnum;
import com.robot.mvc.core.telegram.ActionRequest;

/**
 * 查询车辆速度值
 * 速度值范围：0-100
 *
 * @author Laotang
 */
public class GetSpdRequest extends ActionRequest {

    public GetSpdRequest(String deviceId, String params) {
        super(new Protocol.Builder()
                .deviceId(deviceId)
                .direction(MakerwitEnum.UP_LINK.getValue())
                .cmdKey(CmdKeyEnum.GETSPD.getValue())
                .params(params)
                .build());
    }

    @Override
    public String cmd() {
        return CmdKeyEnum.GETSPD.getValue();
    }
}
