package com.openagv.route;

import cn.hutool.core.util.ClassUtil;
import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import com.openagv.core.AgvContext;
import com.openagv.tools.ToolsKit;

import java.util.*;
import java.util.function.Consumer;

public class RouteHelper {

    private final static Log logger = LogFactory.get();

    public static Map<String,Route> getRoutes() {
        if(AgvContext.getInjectClassObjectSet().isEmpty()) {
            java.util.Objects.requireNonNull(AgvContext.getInjectClassSet(), "要做路由映射的类不能为空");
            AgvContext.getInjectClassSet().forEach(new Consumer<Class<?>>() {
                @Override
                public void accept(Class<?> clazz) {
                    String key = ToolsKit.getControllerName(clazz);
                    Object injectObj = AgvContext.getGuiceInjector().getInstance(clazz);
                    AgvContext.getInjectClassObjectSet().add(injectObj);
                    AgvContext.getRouteMap().put(key.toLowerCase(), new Route(key, injectObj));
                }
            });
        }
        printRouteKey();
        return AgvContext.getRouteMap();
    }

    private static void printRouteKey() {
        List<String> keyList = new ArrayList<>(AgvContext.getRouteMap().keySet());
        Collections.sort(keyList);
        logger.warn("**************** Route Key ****************");
        for (String key : keyList) {
            Route route = AgvContext.getRouteMap().get(key);
            logger.warn(route.toString());
        }
    }
}
