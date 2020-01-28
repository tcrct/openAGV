package com.makerwit.core.responses;

import com.makerwit.core.component.Protocol;
import com.makerwit.numes.CmdKeyEnum;
import com.makerwit.numes.MakerwitEnum;
import com.robot.mvc.core.telegram.ActionResponse;

/**
 * 上报小车手动/自动模式
 * 1为自动，0为手动
 * <p>
 * Created by laotang on 2020/1/28.
 */
public class RptModeResponse extends ActionResponse {

    public RptModeResponse(String deviceId, String param) {
        super(new Protocol.Builder()
                .deviceId(deviceId)
                .direction(MakerwitEnum.UP_LINK.getValue())
                .cmdKey(CmdKeyEnum.RPTMODE.getValue())
                .params(param)
                .build());
    }

    @Override
    public String cmd() {
        return CmdKeyEnum.RPTMODE.getValue();
    }


}
