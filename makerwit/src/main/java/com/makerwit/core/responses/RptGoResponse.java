package com.makerwit.core.responses;

import com.makerwit.core.component.Protocol;
import com.makerwit.numes.CmdKeyEnum;
import com.makerwit.numes.MakerwitEnum;
import com.robot.mvc.core.telegram.ActionResponse;

/**
 * 小车请求继续执行当前任务
 * 按启动触发该指令
 * <p>
 * Created by laotang on 2020/1/28.
 */
public class RptGoResponse extends ActionResponse {

    public RptGoResponse(String deviceId, String param) {
        super(new Protocol.Builder()
                .deviceId(deviceId)
                .direction(MakerwitEnum.UP_LINK.getValue())
                .cmdKey(CmdKeyEnum.RPTGO.getValue())
                .params(param)
                .build());
    }

    @Override
    public String cmd() {
        return CmdKeyEnum.RPTGO.getValue();
    }


}
