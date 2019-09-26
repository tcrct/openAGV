package com.openagv.handlers;

import cn.hutool.core.util.ReflectUtil;
import com.openagv.mvc.BaseController;
import com.openagv.core.interfaces.IHandler;
import com.openagv.core.interfaces.IRequest;
import com.openagv.core.interfaces.IResponse;
import com.openagv.route.Route;
import com.openagv.route.RouteHelper;

public class AccountHandler implements IHandler {

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
        Route route = RouteHelper.getRoutes().get(target);
        java.util.Objects.requireNonNull(route, "根据["+target+"]找不到对应路由映射");
        BaseController controllerObj = (BaseController)route.getInjectController();
        Object resultObj = ReflectUtil.invoke(controllerObj, "handle", NULL_ARGS);
        controllerObj.getRender(resultObj).setContext(request, response).render();
    }

}
