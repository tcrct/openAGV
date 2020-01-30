package com.robot.mvc.model;

import cn.hutool.core.util.ReflectUtil;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Route {

    private String key;
    /**执行的控制器类对象*/
    private Object serviceObj;
    /**服务类对象*/
    private Class<?> serviceClass;
    /**对应的所有公用方法，不包括Object里的公用方法*/
    private Map<String, Method> methodMap = new ConcurrentHashMap<>();

    public Route(Class<?> serviceClass, Map<String, Method> methodMap) {
        this.key =  serviceClass.getName();
        this.serviceObj = ReflectUtil.newInstance(serviceClass);
        this.serviceClass = serviceClass;
        this.methodMap.putAll(methodMap);
    }

    public Route(String key, Object serviceObj) {
        this.key = key;
        this.serviceObj = serviceObj;
        this.serviceClass = serviceObj.getClass();
    }

    public String getKey() {
        return key;
    }

    public Object getServiceObj() {
        return serviceObj;
    }

    public Class<?> getServiceClass() {
        return serviceClass;
    }

    public Map<String, Method> getMethodMap() {
        return methodMap;
    }

    @Override
    public String toString() {
        return "Route{" +
                "key='" + key + '\'' +
                ", class=" + serviceObj.getClass().getName() +
                ", methods=" +methodMap.keySet() +
                '}';
    }
}
