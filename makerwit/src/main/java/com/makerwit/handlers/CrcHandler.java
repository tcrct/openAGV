package com.makerwit.handlers;

import com.makerwit.core.component.Protocol;
import com.makerwit.utils.ProtocolUtil;
import com.robot.mvc.core.exceptions.RobotException;
import com.robot.mvc.core.interfaces.IHandler;
import com.robot.mvc.core.interfaces.IRequest;
import com.robot.mvc.core.interfaces.IResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 验证码处理器
 * 重新计算提交的验证码是否正确
 *
 * Created by laotang on 2020/1/13.
 */
public class CrcHandler implements IHandler {

    private static final Logger LOG = LoggerFactory.getLogger(CrcHandler.class);

    @Override
    public boolean doHandler(String target, IRequest request, IResponse response) throws RobotException {
        Protocol protocol = (Protocol) request.getProtocol();
        String crc = ProtocolUtil.builderCrcString(protocol);
        // 区分大小写
        if (!crc.equals(protocol.getCode())) {
            LOG.error("提交的验证与调度系统计算的不一致，退出处理！");
            return false;
        }
        return true;
    }
}
