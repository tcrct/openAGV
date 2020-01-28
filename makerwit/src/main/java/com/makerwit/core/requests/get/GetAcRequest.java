package com.makerwit.core.requests.get;


import com.makerwit.core.component.Protocol;
import com.makerwit.numes.CmdKeyEnum;
import com.makerwit.numes.MakerwitEnum;
import com.robot.mvc.core.telegram.ActionRequest;

/**
 * 查询RFID卡
 *
 * @author Laotang
 */
public class GetAcRequest extends ActionRequest {

    public GetAcRequest(String deviceId, String params) {
        super(new Protocol.Builder()
                .deviceId(deviceId)
                .direction(MakerwitEnum.UP_LINK.getValue())
                .cmdKey(CmdKeyEnum.GETAC.getValue())
                .params(params)
                .build());
    }

    @Override
    public String cmd() {
        return CmdKeyEnum.GETAC.getValue();
    }


}
