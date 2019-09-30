package com.openagv.core.command;

import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import com.openagv.core.AppContext;
import com.openagv.core.interfaces.IDecomposeTelegram;
import com.openagv.core.interfaces.IRequest;
import com.openagv.core.interfaces.IResponse;
import com.openagv.exceptions.AgvException;
import com.openagv.mvc.RequestTask;
import com.openagv.opentcs.telegrams.OrderRequest;
import com.openagv.opentcs.telegrams.StateRequest;
import com.openagv.tools.ToolsKit;
import io.netty.handler.codec.http.HttpResponseStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

/**
 * 发送命令
 *
 * @author Laotang
 */
public class SendCommand extends Command {

    private static final Log logger = LogFactory.get();
    private static IDecomposeTelegram decomposeTelegram;

    @Override
    public <T> T execute(IRequest request) {
        if(null == decomposeTelegram) {
            decomposeTelegram = AppContext.getAgvConfigure().getDecomposeTelegram();
            java.util.Objects.requireNonNull(decomposeTelegram, "请先实现OpenAgvConfigure类里的getDecomposeTelegram方法");
        }
        if(request instanceof OrderRequest) {
           return (T)sendOrderCommand((OrderRequest)request);
        }
        else if(request instanceof StateRequest) {
            return (T)sendStateCommand((StateRequest)request);
        }
        else {
            throw new AgvException("该请求没实现");
        }
    }

    /**
     * 发送命令请求
     * @param request
     * @return
     */
    private List<IResponse> sendOrderCommand(OrderRequest request) {
        List<IRequest> requestList = decomposeTelegram.handle(request);
        if(ToolsKit.isEmpty(requestList)) {
            throw new AgvException("返回的转换结果集不能为空");
        }
        List<IResponse> responseList = new ArrayList<>(requestList.size());
        for(IRequest requestItem : requestList) {
            responseList.add(sendStateCommand(requestItem));
        }
        return responseList;
    }

    /**
     * 发送车辆移动请求
     * @param request
     * @return
     */
    private IResponse sendStateCommand(IRequest request) {
        IResponse response = null;
        Future<IResponse> futureTask = ThreadUtil.execAsync(new RequestTask(request));
        try {
            response = futureTask.get();
        } catch (Exception e) {
            logger.error("执行任务时出错: {}", e.getMessage());
        }
        return response;
    }



}
