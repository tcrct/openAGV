package com.makerwit.handlers;

import com.openagv.mvc.core.exceptions.AgvException;
import com.openagv.mvc.core.interfaces.IHandler;
import com.openagv.mvc.core.interfaces.IRequest;
import com.openagv.mvc.core.interfaces.IResponse;
import com.openagv.mvc.utils.ToolsKit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 初始验证处理器
 * Created by laotang on 2020/1/13.
 */
public class InitHandler implements IHandler {

    private static final Logger LOG = LoggerFactory.getLogger(InitHandler.class);

    @Override
    public boolean doHandler(String target, IRequest request, IResponse response) throws AgvException {

        if (ToolsKit.isEmpty(target)) {
            LOG.error("请求指令字段不能为空，退出处理！");
            return false;
        }
        if (ToolsKit.isEmpty(request)) {
            LOG.error("请求对象不能为空，退出处理！");
            return false;
        }
        if (ToolsKit.isEmpty(request.getProtocol())) {
            LOG.error("请求协议对象不能为空，退出处理！");
            return false;
        }
        if (ToolsKit.isEmpty(request.getReqType())) {
            LOG.error("请求类型枚举不能为空，退出处理！");
            return false;
        }
        if (ToolsKit.isEmpty(request.getId())) {
            LOG.error("请求ID字段不能为空，退出处理！");
            return false;
        }
        if (response.isResponseTo(request) ) {
            LOG.error("请求ID与返回ID不相等，退出处理！");
            return false;
        }

        return true;
    }
}
