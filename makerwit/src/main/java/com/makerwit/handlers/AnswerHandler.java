package com.makerwit.handlers;

import cn.hutool.core.thread.ThreadUtil;
import com.makerwit.core.component.Protocol;
import com.makerwit.numes.MakerwitEnum;
import com.makerwit.utils.ProtocolUtil;
import com.openagv.mvc.core.exceptions.AgvException;
import com.openagv.mvc.core.interfaces.IHandler;
import com.openagv.mvc.core.interfaces.IRequest;
import com.openagv.mvc.core.interfaces.IResponse;
import com.openagv.mvc.core.telegram.BusinessResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *  请求协议应答回复线程
 *  用于对请求到调试系统的请求进行应答回复
 *
 * @author Laotang
 */
public class AnswerHandler implements IHandler {

    private static final Logger LOG = LoggerFactory.getLogger(AnswerHandler.class);

    @Override
    public boolean doHandler(String target, IRequest request, IResponse response) throws AgvException {
        Protocol protocol = (Protocol) request.getProtocol();

        String direction = protocol.getDirection();
        // 凡是请求上报的(s)，均需要应答回复
        if (MakerwitEnum.UP_LINK.getValue().equals(direction)) {
            ThreadUtil.execute(new Runnable() {
                @Override
                public void run() {
                    Protocol newProtocol = new Protocol.Builder()
                            .deviceId(protocol.getDeviceId())
                            .cmdKey(protocol.getCmdKey())
                            .direction(MakerwitEnum.DOWN_LINK.getValue())
                            .params(protocol.getParams())
                            .build();
                    newProtocol.setCode(ProtocolUtil.builderCrcString(newProtocol));
                    // 发送应答回复
                    String protocolStr = ProtocolUtil.converterString(newProtocol);
                    IResponse businessResponse = new BusinessResponse(request.getId());
                    businessResponse.write(protocolStr);
                    LOG.info("向设备[{}]回复应答报文[{}]", request.getAdapter().getName(), protocolStr);
                    request.getAdapter().sender(businessResponse);
                }
            });
        }
        return true;
    }
}
