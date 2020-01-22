package com.makerwit.core.requests.set;

import com.makerwit.core.component.Protocol;
import com.makerwit.numes.CmdKeyEnum;
import com.makerwit.numes.MakerwitEnum;
import com.openagv.mvc.core.telegram.ActionRequest;

/**
 * 设置循迹方式--手动测试用
 * 0表示中间循迹
 * 1表示左循迹
 * 2表示右循迹
 * 3.左转90度（动力单元固定）
 * 4.右转90度（动力单元固定）
 * 5.180度掉头（动力单元固定）
 * 6.掉头行驶（双向）
 *
 * @author Laotang
 */
public class SetTraRequest extends ActionRequest {

    public SetTraRequest(String deviceId, String param) {
        super(new Protocol.Builder()
                .deviceId(deviceId)
                .direction(MakerwitEnum.UP_LINK.getValue())
                .cmdKey(CmdKeyEnum.SETTRA.getValue())
                .params(param)
                .build());
    }

    @Override
    public String cmd() {
        return CmdKeyEnum.SETTRA.getValue();
    }
}
