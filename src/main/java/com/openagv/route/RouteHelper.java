package com.openagv.route;

import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import com.openagv.core.AppContext;
import com.openagv.tools.ToolsKit;

import java.util.*;
import java.util.function.Consumer;

public class RouteHelper {

    private final static Log logger = LogFactory.get();

    public static Map<String,Route> getRoutes() {
        if(AppContext.getInjectClassObjectSet().isEmpty()) {
            java.util.Objects.requireNonNull(AppContext.getInjectClassSet(), "要做路由映射的类不能为空");
            AppContext.getInjectClassSet().forEach(new Consumer<Class<?>>() {
                @Override
                public void accept(Class<?> clazz) {
                    String key = ToolsKit.getControllerName(clazz);
                    Object injectObj = AppContext.getGuiceInjector().getInstance(clazz);
                    AppContext.getInjectClassObjectSet().add(injectObj);
                    AppContext.getRouteMap().put(key.toLowerCase(), new Route(key, injectObj));
                }
            });
        }
        printRouteKey();
        return AppContext.getRouteMap();
    }

    private static void printRouteKey() {
        List<String> keyList = new ArrayList<>(AppContext.getRouteMap().keySet());
        Collections.sort(keyList);
        logger.warn("**************** Route Key ****************");
        for (String key : keyList) {
            Route route = AppContext.getRouteMap().get(key);
            logger.warn(route.toString());
        }
    }
}
