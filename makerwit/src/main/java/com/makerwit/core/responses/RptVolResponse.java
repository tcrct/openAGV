package com.makerwit.core.responses;


import com.makerwit.core.component.Protocol;
import com.makerwit.numes.CmdKeyEnum;
import com.makerwit.numes.MakerwitEnum;
import com.robot.mvc.core.telegram.ActionResponse;

/**
 * 音量上报
 * Created by laotang on 2020/1/28.
 */
public class RptVolResponse extends ActionResponse {

    public RptVolResponse(String deviceId, String param) {
        super(new Protocol.Builder()
                .deviceId(deviceId)
                .direction(MakerwitEnum.UP_LINK.getValue())
                .cmdKey(CmdKeyEnum.RPTVOL.getValue())
                .params(param)
                .build());
    }

    @Override
    public String cmd() {
        return CmdKeyEnum.RPTVOL.getValue();
    }


}
