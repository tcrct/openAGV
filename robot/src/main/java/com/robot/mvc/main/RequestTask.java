package com.robot.mvc.main;

import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import com.robot.mvc.core.exceptions.RobotException;
import com.robot.mvc.core.interfaces.IRequest;
import com.robot.mvc.core.interfaces.IResponse;
import com.robot.mvc.core.telegram.BaseResponse;
import com.robot.mvc.utils.ToolsKit;

import java.util.concurrent.Callable;

/**
 * 请求处理器，一线程一处理
 * Created by laotang on 2022/1/12.
 */
public class RequestTask implements Callable<IResponse> {

    private static final Log LOG = LogFactory.get();

    private volatile IRequest iRequest = null;
    private volatile IResponse iResponse = null;

    public RequestTask(IRequest iRequest, IResponse iResponse) {
        this.iRequest = iRequest;
        this.iResponse = iResponse;
    }

    @Override
    public IResponse call() {

        if (ToolsKit.isEmpty(iRequest) || ToolsKit.isEmpty(iResponse)) {
            throw new RobotException("RequestTask request or response is null");
        }
        // 执行请求任务
        Main.duang().doTask(iRequest, iResponse);
        return iResponse;
    }
}
