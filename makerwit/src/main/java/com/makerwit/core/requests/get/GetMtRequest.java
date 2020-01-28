package com.makerwit.core.requests.get;

import com.makerwit.core.component.Protocol;
import com.makerwit.numes.CmdKeyEnum;
import com.makerwit.numes.MakerwitEnum;
import com.robot.mvc.core.telegram.ActionRequest;

/**
 * 查询物料请求
 *
 * @author Laotang
 */
public class GetMtRequest extends ActionRequest {

    public GetMtRequest(String deviceId, String params) {
        super(new Protocol.Builder()
                .deviceId(deviceId)
                .direction(MakerwitEnum.UP_LINK.getValue())
                .cmdKey(CmdKeyEnum.GETMT.getValue())
                .params(params)
                .build());
    }

    @Override
    public String cmd() {
        return CmdKeyEnum.GETMT.getValue();
    }
}
