package com.openagv.route;

import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import com.openagv.core.AppContext;
import com.openagv.core.annotations.Controller;
import com.openagv.core.annotations.Service;
import com.openagv.tools.SettingUtils;
import com.openagv.tools.ToolsKit;

import java.util.*;
import java.util.function.Consumer;

public class RouteHelper {

    private final static Log logger = LogFactory.get();

    public static Map<String,Route> getRoutes() {
        String invokeClassType = AppContext.getInvokeClassType();
        if(AppContext.getRouteMap().isEmpty()) {
            java.util.Objects.requireNonNull(AppContext.getInjectClassSet(), "要做路由映射的类不能为空");
            AppContext.getInjectClassSet().forEach(new Consumer<Class<?>>() {
                @Override
                public void accept(Class<?> clazz) {
                    String key = null;
                    Object injectObj = null;
                    if(ToolsKit.SERVICE_FIELD.equalsIgnoreCase(invokeClassType) && ToolsKit.isInjectServiceClass(clazz)) {
                        key = ToolsKit.getInjectClassName(Service.class, clazz).toUpperCase();
                        clazz = clazz.getInterfaces()[0];
                        injectObj = AppContext.getGuiceInjector().getInstance(clazz);
                    }
                    if(ToolsKit.CONTROLLER_FIELD.equalsIgnoreCase(invokeClassType) && ToolsKit.isInjectControllerClass(clazz)){
                        key = ToolsKit.getInjectClassName(Controller.class, clazz).toUpperCase();
                        injectObj = AppContext.getGuiceInjector().getInstance(clazz);
                    }
                    if(null != injectObj) {
                        AppContext.getInjectClassObjectSet().add(injectObj);
                        AppContext.getRouteMap().put(key, new Route(key, injectObj));
                    }
                }
            });
            printRouteKey();
        }
        return AppContext.getRouteMap();
    }

    private static void printRouteKey() {
        List<String> keyList = new ArrayList<>(AppContext.getRouteMap().keySet());
        if(keyList.isEmpty()) {
            throw new NullPointerException("业务逻辑处理类不存在！");
        }
        Collections.sort(keyList);
        logger.warn("**************** Route Key ****************");
        for (String key : keyList) {
            Route route = AppContext.getRouteMap().get(key);
            System.out.println(String.format("route mapping: %s, route: %s", key, route.toString()));
        }
    }
}
