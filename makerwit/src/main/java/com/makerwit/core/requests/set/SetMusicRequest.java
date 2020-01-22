package com.makerwit.core.requests.set;

import com.makerwit.core.component.Protocol;
import com.makerwit.numes.CmdKeyEnum;
import com.makerwit.numes.MakerwitEnum;
import com.openagv.mvc.core.telegram.ActionRequest;

/**
 * 设置音乐
 * 1-6为音量
 * 0关闭音乐
 *
 * @author Laotang
 */
public class SetMusicRequest extends ActionRequest {

    public SetMusicRequest(String deviceId, String param) {
        super(new Protocol.Builder()
                .deviceId(deviceId)
                .direction(MakerwitEnum.UP_LINK.getValue())
                .cmdKey(CmdKeyEnum.SETMUSIC.getValue())
                .params(param)
                .build());
    }

    @Override
    public String cmd() {
        return CmdKeyEnum.SETMUSIC.getValue();
    }
}
