package com.robot.mvc.handlers;

import cn.hutool.core.util.ReflectUtil;
import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import com.robot.mvc.core.exceptions.AgvException;
import com.robot.mvc.core.interfaces.IRequest;
import com.robot.mvc.core.interfaces.IResponse;
import com.robot.mvc.helpers.RouteHelper;
import com.robot.mvc.model.Route;
import com.robot.mvc.utils.ToolsKit;

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
                return emptyRouteOrMehtod(deviceId, target, request, response);
            }
            Method method = route.getMethodMap().get(target.toLowerCase());
            // 如果Service里没有实现该指令对应的方法，则执行公用的duang方法，直接返回响应协议，防止抛出异常
            if (ToolsKit.isEmpty(method)) {
                return emptyRouteOrMehtod(deviceId, target, request, response);
            }
            Object resultObj = ReflectUtil.invoke(route.getServiceObj(), method, request, response);
            if (response.isResponseTo(request)) {
                response.write(resultObj);
            }
        } catch (Exception e) {
            throw new AgvException(e.getMessage(), e);
        }
        return response;
    }

    /***
     * 如果没有实现对应的类或方法时，直接返回response
     * @param deviceId  车辆或设备
     * @param methodName 方法名称
     * @return
     */
    private IResponse emptyRouteOrMehtod(String deviceId,String methodName, IRequest request, IResponse response) {
        if (response.isResponseTo(request) ) {
            LOG.info("{}Service没有实现{}方法，直接返回请求对象协议内容字符串", deviceId, methodName);
            response.write(request.getRawContent());
            return response;
        }
        return null;
    }
}
