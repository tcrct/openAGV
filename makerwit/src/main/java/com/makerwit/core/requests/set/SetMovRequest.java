package com.makerwit.core.requests.set;


import com.makerwit.core.protocol.Protocol;
import com.makerwit.numes.CmdKeyEnum;
import com.makerwit.numes.MakerwitEnum;
import com.makerwit.utils.ProtocolUtil;
import com.openagv.mvc.core.telegram.ActionRequest;

/**
 * 启动--手动测试用
 * 0-缺省值(同上次)
 * 1代表前进,,-1后退
 *
 * @author Laotang
 */
public class SetMovRequest extends ActionRequest {

    public SetMovRequest(String deviceId, String param) {
        super(new Protocol.Builder()
                .deviceId(deviceId)
                .direction(MakerwitEnum.UP_LINK.getValue())
                .cmdKey(CmdKeyEnum.SETMOV.getValue())
                .params(param)
                .build());
    }

    @Override
    public String cmd() {
        return CmdKeyEnum.SETMOV.getValue();
    }

}
