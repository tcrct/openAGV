package com.openagv.handlers;

import cn.hutool.core.util.ReflectUtil;
import com.openagv.core.AppContext;
import com.openagv.core.interfaces.IService;
import com.openagv.mvc.BaseController;
import com.openagv.core.interfaces.IHandler;
import com.openagv.core.interfaces.IRequest;
import com.openagv.core.interfaces.IResponse;
import com.openagv.route.Route;
import com.openagv.route.RouteHelper;
import com.openagv.tools.ToolsKit;

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

    public void doHandler(String target, IRequest request, IResponse response) {
        target = target.toUpperCase();
        Route route = RouteHelper.getRoutes().get(target);
        java.util.Objects.requireNonNull(route, "根据["+target+"]找不到对应路由映射");
        Object object = route.getInjectObject();
        if(ToolsKit.SERVICE_FIELD.equalsIgnoreCase(AppContext.getInvokeClassType())) {
            Object resultObj = ReflectUtil.invoke(object, target, request, response);
            response.write(resultObj);
        }
        else if(ToolsKit.CONTROLLER_FIELD.equalsIgnoreCase(AppContext.getInvokeClassType())) {
            BaseController controllerObj = (BaseController) route.getInjectObject();
            controllerObj.init(request, response);
            Object resultObj = ReflectUtil.invoke(controllerObj, target, request);
            controllerObj.getRender(resultObj).setContext(request, response).render();
        }
    }

}
