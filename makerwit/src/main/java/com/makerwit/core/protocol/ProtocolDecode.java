package com.makerwit.core.protocol;

import com.makerwit.utils.ProtocolUtil;
import com.openagv.mvc.core.interfaces.IProtocol;
import com.openagv.mvc.core.interfaces.IProtocolDecode;

/**
 * 协议解码器
 * Created by laotang on 2020/1/13.
 */
public class ProtocolDecode implements IProtocolDecode {

    @Override
    public IProtocol decode(String message) throws Exception {
        return ProtocolUtil.buildProtocol(message);
    }

}
