package com.openagv.mvc.main;

import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import com.openagv.mvc.core.exceptions.AgvException;
import com.openagv.mvc.core.interfaces.IRequest;
import com.openagv.mvc.core.interfaces.IResponse;
import com.openagv.mvc.core.telegram.BaseResponse;
import com.openagv.mvc.utils.ToolsKit;

import java.util.concurrent.Callable;

/**
 * 请求处理器，一线程一处理
 * Created by laotang on 2022/1/12.
 */
public class RequestTask implements Callable<IResponse> {

    private static final Log LOG = LogFactory.get();

    private volatile IRequest iRequest = null;
    private volatile IResponse iResponse = null;

    public RequestTask(IRequest iRequest) {
        this.iRequest = iRequest;
    }

    @Override
    public IResponse call() {
        iResponse = new BaseResponse(iRequest.getId());
        if (ToolsKit.isEmpty(iRequest) || ToolsKit.isEmpty(iResponse)) {
            throw new AgvException("RequestTask request or response fail");
        }
        // 执行请求任务
        try {
            Main.duang().doTask(iRequest, iResponse);
        }  catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
        return iResponse;
    }
}
