package com.makerwit.core.component;

import com.makerwit.utils.ProtocolUtil;
import com.robot.mvc.core.interfaces.IProtocol;
import com.robot.mvc.core.interfaces.IProtocolDecode;

/**
 * 协议解码器
 * Created by laotang on 2020/1/13.
 */
public class ProtocolDecode implements IProtocolDecode {

    private static class ProtocolDecodeHandlerHolder {
        private static final ProtocolDecode INSTANCE = new ProtocolDecode();
    }
    private ProtocolDecode() {
    }
    public static final ProtocolDecode duang() {
        return ProtocolDecode.ProtocolDecodeHandlerHolder.INSTANCE;
    }

    @Override
    public IProtocol decode(String message) throws Exception {
        return ProtocolUtil.buildProtocol(message);
    }

}
