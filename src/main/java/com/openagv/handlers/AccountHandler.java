package com.openagv.handlers;

import cn.hutool.core.util.ReflectUtil;
import com.openagv.core.AppContext;
import com.openagv.exceptions.AgvException;
import com.openagv.mvc.BaseController;
import com.openagv.core.interfaces.IRequest;
import com.openagv.core.interfaces.IResponse;
import com.openagv.route.Route;
import com.openagv.route.RouteHelper;
import com.openagv.tools.ToolsKit;

import java.lang.reflect.Method;
import java.util.Optional;

/**
 * 访问处理器
 */
public class AccountHandler {

    private static final Object[] NULL_ARGS = new Object[0];		// 默认参数

    private static class AccountHandlerHolder {
        private static final AccountHandler INSTANCE = new AccountHandler();
    }
    private AccountHandler() {
    }
    public static final AccountHandler duang() {
        return AccountHandlerHolder.INSTANCE;
    }


    /**
     * 执行方法
     * @param deviceId  设备/车辆ID
     * @param target    功能指令，即方法
     * @param request 请求对象
     * @param response 响应对象
     * @throws Exception
     */
    public void doHandler(String deviceId, String target, IRequest request, IResponse response) throws Exception{
        try {
            Route route = Optional.ofNullable(RouteHelper.getRoutes().get(deviceId)).orElseThrow(NullPointerException::new);
            Method method = route.getMethodMap().get(target.toLowerCase());
            // 如果Service里没有实现该指令对应的方法，则执行公用的duang方法，直接返回响应协议，防止抛出异常
            if(ToolsKit.isEmpty(method)) {
                method = route.getMethodMap().get("duang");
            }
            Object resultObj = ReflectUtil.invoke(route.getInjectObject(), method, request, response);
            response.write(resultObj);
        } catch (NullPointerException npe) {
            throw new AgvException("找不到对应的["+deviceId+"]类，请确保类存在: " + npe.getMessage(),npe);
        } catch (Exception e) {
            throw new AgvException(e.getMessage(),e);
        }
}

    public void doHandler2(String target, IRequest request, IResponse response) throws Exception{
        target = target.toUpperCase();
        Route route = RouteHelper.getRoutes().get(target);
        java.util.Objects.requireNonNull(route, "根据["+target+"]找不到对应路由映射");
        Object object = route.getInjectObject();
        java.util.Objects.requireNonNull(route, "根据["+target+"]找不到对应处理类对象");
        try {
            if (ToolsKit.SERVICE_FIELD.equalsIgnoreCase(AppContext.getInvokeClassType())) {
                Object resultObj = ReflectUtil.invoke(object, target, request, response);
                response.write(resultObj);
            } else if (ToolsKit.CONTROLLER_FIELD.equalsIgnoreCase(AppContext.getInvokeClassType())) {
                BaseController controllerObj = (BaseController) route.getInjectObject();
                controllerObj.init(request, response);
                Object resultObj = ReflectUtil.invoke(controllerObj, target, request);
                controllerObj.getRender(resultObj).setContext(request, response).render();
            }
        } catch (Exception e) {
            throw new AgvException(e.getMessage(),e);
        }
    }

}
