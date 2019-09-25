package com.openagv.handlers;

import com.openagv.core.interfaces.IHandler;
import com.openagv.route.Route;
import com.openagv.route.RouteHelper;

public class AccountHandler implements IHandler {

    public static void doHandler(String cmd){
        Route route = RouteHelper.getRoutes().get(cmd);
        java.util.Objects.requireNonNull(route, "根据["+cmd+"]找不到对应路由映射");
        route.getInjectObj();
    }

}
