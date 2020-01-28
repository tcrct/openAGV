package com.makerwit.core.responses;


import com.makerwit.core.component.Protocol;
import com.makerwit.numes.CmdKeyEnum;
import com.makerwit.numes.MakerwitEnum;
import com.robot.mvc.core.telegram.ActionResponse;

/**
 * 上报动作到位信息
 * 预停车指令时用
 * <p>
 * Created by laotang on 2020/1/28.
 */
public class RptrtpResponse extends ActionResponse {

    public RptrtpResponse(String deviceId, String param) {
        super(new Protocol.Builder()
                .deviceId(deviceId)
                .direction(MakerwitEnum.UP_LINK.getValue())
                .cmdKey(CmdKeyEnum.RPTRTP.getValue())
                .params(param)
                .build());
    }

    @Override
    public String cmd() {
        return CmdKeyEnum.RPTRTP.getValue();
    }


}
