package com.openagv.core.helper;

import cn.hutool.core.util.ReflectUtil;
import com.openagv.core.interfaces.IService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ServiceBean {

    private static final Logger logger = LoggerFactory.getLogger(ServiceBean.class);

    /**服务类对象*/
    private Class<?> serviceClass;
    private IService serviceObj;
    /**对应的所有方法*/
    private Map<String, Method> methodMap = new ConcurrentHashMap<>();

    public ServiceBean() {

    }

    public ServiceBean(Class<?> serviceClass, Map<String, Method> methodMap) {
        this.serviceClass = serviceClass;
        setServiceObj();
        this.methodMap = methodMap;
    }

    public Class<?> getServiceClass() {
        return serviceClass;
    }

    private void setServiceObj() {
        try {
            this.serviceObj = (IService) ReflectUtil.newInstance(serviceClass);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }
    public void setServiceClass(Class<?> serviceClass) {
        this.serviceClass = serviceClass;
    }

    public IService getServiceObj() {
        return serviceObj;
    }

    public Map<String, Method> getMethodMap() {
        return methodMap;
    }

    public void setMethodMap(Map<String, Method> methodMap) {
        this.methodMap = methodMap;
    }
}
