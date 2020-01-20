package com.makerwit.core.requests.set;

import com.makerwit.core.protocol.Protocol;
import com.makerwit.numes.CmdKeyEnum;
import com.makerwit.numes.MakerwitEnum;
import com.openagv.mvc.core.telegram.ActionRequest;

/**
 *
 * @author Laotang
 */
public class SetAvoidRequest extends ActionRequest {

    public SetAvoidRequest(String deviceId, String param) {
        super(new Protocol.Builder()
                .deviceId(deviceId)
                .direction(MakerwitEnum.UP_LINK.getValue())
                .cmdKey(CmdKeyEnum.SETAVOID.getValue())
                .params(param)
                .build());
    }

    @Override
    public String cmd() {
        return CmdKeyEnum.SETAVOID.getValue();
    }
}
