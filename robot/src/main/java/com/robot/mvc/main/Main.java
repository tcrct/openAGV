package com.robot.mvc.main;

import com.robot.AgvContext;
import com.robot.mvc.core.interfaces.IHandler;
import com.robot.mvc.core.interfaces.IProtocol;
import com.robot.mvc.core.interfaces.IRequest;
import com.robot.mvc.core.interfaces.IResponse;
import com.robot.mvc.core.telegram.BusinessRequest;
import com.robot.mvc.core.telegram.MoveRequest;
import com.robot.mvc.handlers.TaskHandler;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.Objects;

/**
 * 调度系统主入口
 *
 * Created by laotang on 2020/1/12.
 */
public class Main {

    private final static Logger LOG = LoggerFactory.getLogger(Main.class);


    private static Main MAIN = new Main();

    private Main() {

    }

    public static Main duang() {
        return MAIN;
    }

    public void doTask(IRequest request, IResponse response) throws Exception {
        try {
            Objects.requireNonNull(request, "request is null");
            Objects.requireNonNull(response, "response is null");
            IProtocol protocol = Objects.requireNonNull(request.getProtocol(), "response is null");
            String target = java.util.Objects.requireNonNull(protocol.getCmdKey(), "协议动作指令值不能为空，必须设置，该值用于反射调用方法");
            if(doBeforeHandler(target, request, response)) {
                TaskHandler.duang().doHandler(target, request, response);
            }
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            //设置为错误500状态
            response.setStatus(HttpResponseStatus.INTERNAL_SERVER_ERROR.code());
            response.write(e.getMessage());
        }
    }

    /**
     * 执行Controller方法前的前置处理器
     * 抛出异常中止执行
     *
     * @param target 协议指令
     * @param request   请求对象
     * @param response 返回对象
     */
    private boolean doBeforeHandler(String target, IRequest request, IResponse response) throws Exception {

        if(AgvContext.getBeforeHeandlerList().isEmpty()) {
            return true;
        }

        // 如果是MoveRequest的请求，属于openTCS发起的请求，作直接跳过的特殊处理
        if(request instanceof MoveRequest){
            return true;
        }

        if(request instanceof BusinessRequest) {
            for (Iterator<IHandler> it = AgvContext.getBeforeHeandlerList().iterator(); it.hasNext(); ) {
                boolean isNextHandle = it.next().doHandler(target, request, response);
                if(!isNextHandle){
                    return false;
                }
            }
        }

        return true;
    }
}
