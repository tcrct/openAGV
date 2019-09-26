package com.openagv.mvc;

import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import com.openagv.core.Main;
import com.openagv.core.interfaces.IRequest;
import com.openagv.core.interfaces.IResponse;
import com.openagv.exceptions.AgvException;
import com.openagv.tools.ToolsKit;

import java.util.concurrent.Callable;

/**
 * 请求处理器，一线程一处理
 * Created by laotang on 2018/6/7.
 */
public class RequestTask implements Callable<IResponse> {

    private static final Log logger = LogFactory.get();

    private IRequest iRequest = null;
    private IResponse iResponse = null;

    public RequestTask(IRequest iRequest) {
        this.iRequest = iRequest;
    }

    @Override
    public IResponse call() {
        try {
            iResponse = Response.build(iRequest);
        } catch (Exception e){
            logger.error(e.getMessage(), e);
        }
        if (ToolsKit.isEmpty(iRequest) || ToolsKit.isEmpty(iResponse)) {
            throw new AgvException("RequestTask request or response fail");
        }
        // 执行请求任务
        Main.doTask(iRequest, iResponse);
        return iResponse;
    }
}
