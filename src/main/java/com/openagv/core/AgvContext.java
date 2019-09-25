package com.openagv.core;

import cn.hutool.core.util.ReflectUtil;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.openagv.route.Route;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

/**
 *
 * @author Laotang
 */
public class AgvContext {

    /**要进行依赖反转的类*/
    private final static Set<Class<?>> INJECT_CLASS_SET = new HashSet<>();
    /**要进行依赖反转的对象*/
    private final static Set<Object> INJECT_OBJECT_SET = new HashSet<>();
    /**路由映射*/
    private final static Map<String, Route> ROUTE_MAP = new HashMap<>();

    private static Injector injector;

    private final static Set<Module> MODULES = new HashSet<>();


    public static void setGuiceInjector(Injector injector) {
        AgvContext.injector = injector;
    }

    public static Injector getGuiceInjector() {
        return injector;
    }

    public static Set<Module> getModules() {
        return MODULES;
    }

    public static Set<Class<?>> getInjectClassSet() {
        return INJECT_CLASS_SET;
    }

    public static Set<Object> getInjectClassObjectSet() {
        if(INJECT_OBJECT_SET.isEmpty()){
            getInjectClassSet().forEach(new Consumer<Class<?>>() {
                @Override
                public void accept(Class<?> clazz) {
                    Object injectObj = AgvContext.getGuiceInjector().getInstance(clazz);
                    String key ="";
                    ROUTE_MAP.put(key, new Route(key, injectObj));
                    INJECT_OBJECT_SET.add(injectObj);
                }
            });
        }
        return INJECT_OBJECT_SET;
    }


    public static Map<String, Route> getRouteMap() {
        return ROUTE_MAP;
    }
}
