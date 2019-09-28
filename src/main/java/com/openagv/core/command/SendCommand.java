package com.openagv.core.command;

import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import com.openagv.core.AgvResult;
import com.openagv.core.AppContext;
import com.openagv.core.interfaces.IRequest;
import com.openagv.core.interfaces.IResponse;
import com.openagv.exceptions.AgvException;
import com.openagv.mvc.RequestTask;
import com.openagv.opentcs.model.Telegram;
import com.openagv.tools.ToolsKit;

import java.util.List;
import java.util.concurrent.Future;

/**
 * 发送命令
 *
 * @author Laotang
 */
public class SendCommand extends Command {

    private static final Log logger = LogFactory.get();

    @Override
    public AgvResult execute(Telegram telegram) {
        List<IRequest> orderList = AppContext.getTelegram().handle(telegram);
        if(ToolsKit.isEmpty(orderList)) {
            throw new AgvException("返回的转换结果集不能为空");
        }
        for(IRequest request : orderList) {
            IResponse response = null;
            Future<IResponse> futureTask = ThreadUtil.execAsync(new RequestTask(request));
            try {
                response = futureTask.get();
            } catch (Exception e) {
                e.printStackTrace();
                logger.error("执行任务时出错: {}", e.getMessage());
            }
            return new AgvResult(request ,response);
        }
        return null;
    }

}
