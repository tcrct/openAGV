package com.openagv.core;

import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import com.openagv.core.interfaces.IHandler;
import com.openagv.core.interfaces.IRequest;
import com.openagv.core.interfaces.IResponse;
import com.openagv.handlers.AccountHandler;
import com.openagv.mvc.ExceptionController;

import java.util.Iterator;

/**
 * 系统入口
 *
 * @author Laotang
 */
public class Main {

    private final static Log logger = LogFactory.get();

    public static void doTask(IRequest request, IResponse response) {
        String target = getResourcePath(request);
        try {
            doBeforeHandler(target, request, response);
            AccountHandler.duang().doHandler(target, request, response);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("执行任务[{}]时出错: {} {}", target, e.getMessage());
            ExceptionController.build().getRender(e.getMessage()).setContext(request, response).render();
        }
        // 另起线程处理
        ThreadUtil.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    doAfterHandler(target, request, response);
                } catch (Exception e) {
                    logger.error("执行后置处理器时发生异常: {}, {} ", e.getMessage(), e);
                }
            }
        });
    }

    private static String getResourcePath(IRequest request) {
        return request.getRequestURI();
    }

    /**
     * 执行Controller方法前的前置处理器
     * 抛出异常中止执行
     *
     * @param target    目标路径
     * @param request   请求对象
     * @param response 返回对象
     */
    private static void doBeforeHandler(String target, IRequest request, IResponse response) throws Exception {
        if(AppContext.getBeforeHeandlerList().isEmpty()) {
            return;
        }
        for (Iterator<IHandler> it = AppContext.getBeforeHeandlerList().iterator(); it.hasNext(); ) {
            it.next().doHandler(target, request, response);
        }
    }

    /**
     * 执行Controller方法前的后置处理器
     * 抛出异常只作log日志记录，不影响数据返回
     *
     * @param target    目标路径
     * @param request   请求对象
     * @param response 返回对象
     */
    private static void doAfterHandler(String target, IRequest request, IResponse response) throws Exception{
        if(AppContext.getAfterHeandlerList().isEmpty()) {
            return;
        }
        for (Iterator<IHandler> it = AppContext.getAfterHeandlerList().iterator(); it.hasNext(); ) {
            it.next().doHandler(target, request, response);
        }
    }

}
