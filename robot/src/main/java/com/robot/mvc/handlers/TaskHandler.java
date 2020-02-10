package com.robot.mvc.handlers;

import cn.hutool.core.util.ReflectUtil;
import cn.hutool.http.HttpStatus;
import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import com.robot.RobotContext;
import com.robot.mvc.core.exceptions.ExceptionEnums;
import com.robot.mvc.core.exceptions.RobotException;
import com.robot.mvc.core.interfaces.IRequest;
import com.robot.mvc.core.interfaces.IResponse;
import com.robot.mvc.core.telegram.BaseResponse;
import com.robot.mvc.helpers.RouteHelper;
import com.robot.mvc.model.Route;
import com.robot.mvc.utils.RobotUtil;
import com.robot.mvc.utils.ToolsKit;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by laotang on 2020/1/12.
 */
public class TaskHandler {

    private static final Log LOG = LogFactory.get();

    private static final Object[] NULL_ARGS = new Object[0];		// 默认参数

    private static class TaskHandlerHolder {
        private static final TaskHandler INSTANCE = new TaskHandler();
    }
    private TaskHandler() {
    }
    public static final TaskHandler duang() {
        return TaskHandlerHolder.INSTANCE;
    }


    /***
     * 执行任务处理
     * @param target 方法名
     * @param request 请求对象
     * @param response 返回对象
     * @throws Exception
     */
    public IResponse doHandler(String target, IRequest request, IResponse response) throws Exception{
        String deviceId = request.getProtocol().getDeviceId();
        try {
            Route route = RouteHelper.duang().getRoutes().get(deviceId);
            if (null == route) {
                return emptyRouteOrMehtod(deviceId, target, request, (BaseResponse) response);
            }
            Method method = route.getMethodMap().get(target.toLowerCase());
            // 如果Service里没有实现该指令对应的方法，则执行公用的duang方法，直接返回响应协议，防止抛出异常
            if (ToolsKit.isEmpty(method)) {
                return emptyRouteOrMehtod(deviceId, target, request, (BaseResponse) response);
            }
            Object resultObj = ReflectUtil.invoke(route.getServiceObj(), method, request, response);
            // 如果是同一个请求响应单元并且rawContent值为空，则写入响应对象
            if (response.isResponseTo(request) && ToolsKit.isEmpty(response.getRawContent())) {
                response.write(resultObj);
            }
            // 如果没有设置响应内容，则抛出异常
            if (ToolsKit.isEmpty(response.getRawContent())) {
                throw new RobotException(ExceptionEnums.RESPONSE_RAW_NULL);
            }
        } catch (Exception e) {
            if (e instanceof InvocationTargetException) {
                InvocationTargetException ite = (InvocationTargetException) e;
                Throwable t = ite.getTargetException();// 获取目标异常
                throw new RobotException(t.getMessage(), t);
            } else {
                throw new RobotException(e.getMessage(), e);
            }
        }
        return response;
    }

    /***
     * 如果没有实现对应的类或方法时，直接返回response，
     * 一般情况下都是需要发送到客户端，但不进入到适配器操作的指令才允许为空
     *
     *
     * @param deviceId  车辆或设备
     * @param methodName 方法名称
     * @return
     */
    private IResponse emptyRouteOrMehtod(String deviceId, String methodName, IRequest request, BaseResponse response) {
        if (RobotUtil.isMoveRequest(request)) {
            throw new RobotException("移动请求必须要实现协议指令对应的[" + RobotUtil.getMoveProtocolKey() + "]方法！");
        }

        if (response.isResponseTo(request) ) {
            LOG.info("{}Service没有实现{}方法，直接返回请求对象协议内容字符串", deviceId, methodName);
            response.write(request.getRawContent());
            response.setStatus(HttpStatus.HTTP_OK);
            if (request.isNeedAdapterOperation()) {
                response.setNeedAdapterOperation(true);
            }
            if (request.isNeedSend()) {
                response.setNeedSend(true);
            }
            return response;
        }
        return null;
    }
}
