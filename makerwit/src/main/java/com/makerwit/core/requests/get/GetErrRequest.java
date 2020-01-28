package com.makerwit.core.requests.get;


import com.makerwit.core.component.Protocol;
import com.makerwit.numes.CmdKeyEnum;
import com.makerwit.numes.MakerwitEnum;
import com.robot.mvc.core.telegram.ActionRequest;

/**
 * 查询状态信息
 *
 * @author Laotang
 */
public class GetErrRequest extends ActionRequest {

    public GetErrRequest(String deviceId, String params) {
        super(new Protocol.Builder()
                .deviceId(deviceId)
                .direction(MakerwitEnum.UP_LINK.getValue())
                .cmdKey(CmdKeyEnum.GETERR.getValue())
                .params(params)
                .build());
    }

    @Override
    public String cmd() {
        return CmdKeyEnum.GETERR.getValue();
    }
}
