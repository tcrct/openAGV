package com.openagv.core.command;

import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import com.openagv.core.interfaces.IDecomposeTelegram;
import com.openagv.core.interfaces.IRequest;
import com.openagv.core.interfaces.IResponse;
import com.openagv.exceptions.AgvException;
import com.openagv.mvc.RequestTask;
import com.openagv.opentcs.telegrams.StateRequest;
import com.openagv.tools.ToolsKit;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

/**
 * 发送命令
 *
 * @author Laotang
 */
public class SendCommand implements Command {

    private static final Log logger = LogFactory.get();

    private static final SendCommand sendCommand = new SendCommand();
    public static SendCommand duang() {
        return sendCommand;
    }
    private SendCommand(){}


    @Override
    public <T> T execute(Object object) {
        if(object instanceof List) {
           return (T)sendOrderCommand((List<IRequest>)object);
        }
        else if(object instanceof StateRequest) {
            return (T)sendStateCommand((StateRequest)object);
        }
        else {
            throw new AgvException("该请求没实现");
        }
    }

    /**
     * 发送命令请求
     * @param requestList
     * @return
     */
    private List<IResponse> sendOrderCommand(List<IRequest> requestList) {
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
