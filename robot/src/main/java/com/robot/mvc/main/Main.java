package com.robot.mvc.main;

import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.ObjectUtil;
import com.robot.RobotContext;
import com.robot.config.Application;
import com.robot.mvc.core.enums.ReqType;
import com.robot.mvc.core.exceptions.RobotException;
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

    public void doTask(IRequest request, IResponse response) {
        Objects.requireNonNull(request, "request is null");
        Objects.requireNonNull(response, "response is null");
        IProtocol protocol = Objects.requireNonNull(request.getProtocol(), "response is null");
        String target = java.util.Objects.requireNonNull(protocol.getCmdKey(), "协议动作指令值不能为空，必须设置，该值用于反射调用方法");
        try {
            if(doBeforeHandler(target, request, response)) {
                TaskHandler.duang().doHandler(target, request, response);
            }
        } catch (Exception e) {
            //设置为错误500状态, 这里只捕捉，不抛出异常，让doAfterHandler继续执行
            response.setStatus(HttpResponseStatus.INTERNAL_SERVER_ERROR.code());
            response.write(e.getMessage());
            LOG.error("Main.doTask时出错: " + e.getMessage(), e);
        }
        doAfterHandler(target, request, response);
    }

    /**
     * 执行TaskHandler前的前置处理器
     * 抛出异常中止执行，返回false，则丢弃该次请求
     *
     * @param target 协议指令
     * @param request   请求对象
     * @param response 返回对象
     */
    private boolean doBeforeHandler(String target, IRequest request, IResponse response) throws Exception {

        if (Application.BEFORE_HEANDLER_LIST.isEmpty()) {
            return true;
        }

        ReqType reqType = request.getReqType();
        // 如果是ActionRequest, MoveRequest的请求，属于openTCS发起的请求，作直接跳过的特殊处理
        if (ReqType.MOVE.equals(reqType) || ReqType.ACTION.equals(reqType)) {
            return true;
        }
        if (ReqType.BUSINESS.equals(reqType)) {
            for (Iterator<IHandler> it = Application.BEFORE_HEANDLER_LIST.iterator(); it.hasNext(); ) {
                boolean isNextHandle = it.next().doHandler(target, request, response);
                if (!isNextHandle) {
                    // 程序终止处理链执行，直接丢弃该次请求
                    return false;
                }
            }
        }
        // 抛出异常
        throw new RobotException("该请求没有设置请求类型[reqType]，请先设置！");
    }

    /**
     * 执行TaskHandler后的后置处理器
     *
     * @param target   协议指令
     * @param request  请求对象
     * @param response 返回对象
     */
    private void doAfterHandler(String target, IRequest request, IResponse response) {
        for (Iterator<IHandler> it = Application.AFTER_HEANDLER_LIST.iterator(); it.hasNext(); ) {
            it.next().doHandler(target, request, response);
        }
    }
}
