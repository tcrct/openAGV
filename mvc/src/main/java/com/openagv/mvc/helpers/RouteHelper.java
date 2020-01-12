package com.openagv.mvc.helpers;

import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import com.openagv.mvc.core.annnotations.Service;
import com.openagv.mvc.core.exceptions.AgvException;
import com.openagv.mvc.model.Route;
import com.openagv.mvc.utils.ToolsKit;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 路由帮助器
 *
 * @author Laotang
 */
public class RouteHelper {

    private final static Log LOG = LogFactory.get();

    private final static Lock lock = new ReentrantLock();
    private static Set<String> excludedMethodName = null;
    /**key为车辆或设备的ID标识符*/
    private static Map<String,Route> ROUTE_MAP = new HashMap<>();

    private static RouteHelper ROUTE_HELPER = null;
    public static RouteHelper duang() {
        try {
            lock.lock();
            if (null == ROUTE_HELPER) {
                ROUTE_HELPER = new RouteHelper();
            }
        } finally {
            lock.unlock();
        }
        return ROUTE_HELPER;
    }

    private RouteHelper() {
        if(ROUTE_MAP.isEmpty()) {
            if (null == excludedMethodName) {
                excludedMethodName = ToolsKit.buildExcludedMethodName();
            }
            List<Class<?>> serviceClassList = ClassHelper.duang().getServiceClassList();
            if (ToolsKit.isEmpty(serviceClassList)) {
                throw new AgvException( "业务逻辑处理类不能为空");
            }
            for (Class<?> serviceClass : serviceClassList) {
                Method[] methodArray = serviceClass.getMethods();
                List<Method> methodList = new ArrayList<>();
                for (Method method : methodArray) {
                    if (!ToolsKit.isPublicMethod(method.getModifiers()) ||
                            excludedMethodName.contains(method.getName())) {
                        continue;
                    }
                    methodList.add(method);
                }
                if (ToolsKit.isNotEmpty(methodList)) {
                    Service serviceAnnoy = serviceClass.getAnnotation(Service.class);
                    String key = serviceAnnoy.value();
                    if (ToolsKit.isEmpty(key)) {
                        int endIndex = serviceClass.getSimpleName().toLowerCase().indexOf("service");
                        if (endIndex > -1) {
                            key = serviceClass.getSimpleName().substring(0, endIndex);
                        }
                    }
                    Map<String, Method> methodMap = new HashMap<>();
                    for (Method method : methodList) {
                        methodMap.put(method.getName().toLowerCase(), method);
                    }
                    ROUTE_MAP.put(key, new Route(serviceClass, methodMap));
                }
            }
            printRouteKey();
        }
    }



    public static Map<String,Route> getRoutes() {
        return ROUTE_MAP;
    }

    private  void printRouteKey() {
        List<String> keyList = new ArrayList<>(ROUTE_MAP.keySet());
        if(keyList.isEmpty()) {
            throw new NullPointerException("业务逻辑处理类不存在！");
        }
        Collections.sort(keyList);
        LOG.warn("**************** Route Key ****************");
        for (String key : keyList) {
            Route route = ROUTE_MAP.get(key);
            LOG.info(String.format("route mapping: %s, route: %s", key, route.toString()));
        }
    }
}
