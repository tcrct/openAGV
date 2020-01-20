package com.makerwit.core.requests.set;

import com.makerwit.core.protocol.Protocol;
import com.makerwit.numes.CmdKeyEnum;
import com.makerwit.numes.MakerwitEnum;
import com.openagv.mvc.core.telegram.ActionRequest;

/**
 * 预停车指令
 * 冒号分隔,,第1位表示卡号；第2位标识胶贴号为0时不起作用,,第3位表示备用标识胶贴号为0时不起作用
 *
 * @author Laotang
 */
public class SetRtpRequest extends ActionRequest {

    public SetRtpRequest(String deviceId, String param) {
        super(new Protocol.Builder()
                .deviceId(deviceId)
                .direction(MakerwitEnum.UP_LINK.getValue())
                .cmdKey(CmdKeyEnum.SETRTP.getValue())
                .params(param)
                .build());
    }

    @Override
    public String cmd() {
        return "setrtp";
    }
}
