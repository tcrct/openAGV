package com.makerwit.core.component;

import com.makerwit.utils.ProtocolUtil;
import com.robot.mvc.core.interfaces.IProtocol;
import com.robot.mvc.core.interfaces.IProtocolMatcher;

/**
 * 协议编码解码器
 * Created by laotang on 2020/1/13.
 */
public class ProtocolMatcher implements IProtocolMatcher {

    private static class ProtocolDecodeHandlerHolder {
        private static final ProtocolMatcher INSTANCE = new ProtocolMatcher();
    }

    private ProtocolMatcher() {
    }

    public static final ProtocolMatcher duang() {
        return ProtocolMatcher.ProtocolDecodeHandlerHolder.INSTANCE;
    }

    @Override
    public IProtocol encode(String message) throws Exception {
        return ProtocolUtil.buildProtocol(message);
    }

    @Override
    public String decode(IProtocol protocol) throws Exception {
        return ProtocolUtil.converterString((Protocol) protocol);
    }

}
