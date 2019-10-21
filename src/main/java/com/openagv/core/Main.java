package com.openagv.core;

import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import com.openagv.core.interfaces.IHandler;
import com.openagv.core.interfaces.IRequest;
import com.openagv.core.interfaces.IResponse;
import com.openagv.handlers.AccountHandler;
import com.openagv.mvc.ExceptionController;
import com.openagv.opentcs.telegrams.OrderRequest;
import com.openagv.opentcs.telegrams.StateRequest;
import com.openagv.tools.ToolsKit;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.apache.log4j.Logger;

import java.util.Iterator;

/**
 * 系统入口
 *
 * @author Laotang
 */
public class Main {

    private final static Logger logger = Logger.getLogger(Main.class);

    public static void doTask(IRequest request, IResponse response) {
        try {
            if(doBeforeHandler(request, response)) {
                java.util.Objects.requireNonNull(request.getCmdKey(), "target值不能为空，必须设置，该值用于反射调用方法");
                AccountHandler.duang().doHandler(request.getCmdKey(), request, response);
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            //设置为错误500状态
            response.setStatus(HttpResponseStatus.INTERNAL_SERVER_ERROR.code());
            if(ToolsKit.SERVICE_FIELD.equalsIgnoreCase(AppContext.getInvokeClassType())) {
                response.write(e.getMessage());
            } else if(ToolsKit.CONTROLLER_FIELD.equalsIgnoreCase(AppContext.getInvokeClassType())) {
                ExceptionController.build().getRender(e.getMessage()).setContext(request, response).render();
            }
        }
        // 如果有后置处理器，则另起线程处理
        if(!AppContext.getAfterHeandlerList().isEmpty()) {
            ThreadUtil.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        // TODO 这里的request ,response是否需要copy
//                        IRequest copyRequest = ObjectUtil.cloneByStream(request);
//                        IResponse copyResponse = ObjectUtil.cloneByStream(response);
//                        doAfterHandler(copyRequest, copyResponse);
                        doAfterHandler(request, response);
                    } catch (Exception e) {
                        logger.error("执行后置处理器时发生异常: "+ e.getMessage(), e);
                    }
                }
            });
        }
    }

    /**
     * 执行Controller方法前的前置处理器
     * 抛出异常中止执行
     *
     * @param request   请求对象
     * @param response 返回对象
     */
    private static boolean doBeforeHandler(IRequest request, IResponse response) throws Exception {
        if(AppContext.getBeforeHeandlerList().isEmpty()) {
            return true;
        }
        // 如果是StateRequest的请求，属于openTCS发起的请求，作直接跳过的特殊处理
        if(request instanceof StateRequest){
            return true;
        }
        if(request instanceof OrderRequest) {
            for (Iterator<IHandler> it = AppContext.getBeforeHeandlerList().iterator(); it.hasNext(); ) {
                boolean isNextHandle = it.next().doHandler(request, response);
                if(!isNextHandle){
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * 执行Controller方法前的后置处理器
     * 抛出异常只作log日志记录，不影响数据返回
     *
     * @param request   请求对象
     * @param response 返回对象
     */
    private static void doAfterHandler(IRequest request, IResponse response) throws Exception{
        if(AppContext.getAfterHeandlerList().isEmpty()) {
            return;
        }
        for (Iterator<IHandler> it = AppContext.getAfterHeandlerList().iterator(); it.hasNext(); ) {
            it.next().doHandler(request, response);
        }
    }

}
