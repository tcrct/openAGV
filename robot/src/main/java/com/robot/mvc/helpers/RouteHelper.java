package com.robot.mvc.helpers;

import cn.hutool.core.util.ReflectUtil;
import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import com.robot.mvc.core.annnotations.Action;
import com.robot.mvc.core.annnotations.Controller;
import com.robot.mvc.core.annnotations.Service;
import com.robot.mvc.core.interfaces.IAction;
import com.robot.mvc.model.Route;
import com.robot.utils.ToolsKit;
import org.opentcs.kernel.extensions.servicewebapi.console.ControllerFactory;
import org.opentcs.kernel.extensions.servicewebapi.console.IController;

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
    /**
     * Service类Map集合，key为车辆ID标识符
     */
    private static Map<String, Route> SERVICE_ROUTE_MAP = new HashMap<>();
    /**
     * Action类Map集合，key为动作指令集标识符
     */
    private static Map<String, Route> ACTION_ROUTE_MAP = new HashMap<>();
    /**
     * Controller类Map集合，key为Controller文件名去除Controller部份
     */
    private static Map<String, Route> CONTROLLER_ROUTE_MAP = new HashMap<>();

    public static Map<String, Route> getControllerRouteMap() {
        return CONTROLLER_ROUTE_MAP;
    }

    public static Map<String, Route> getServiceRouteMap() {
        return SERVICE_ROUTE_MAP;
    }

    public static Map<String, Route> getActionRouteMap() {
        return ACTION_ROUTE_MAP;
    }

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
        routeController();
        routeService();
        routeAction();
    }

    private void routeController() {
        if (CONTROLLER_ROUTE_MAP.isEmpty()) {
            if (null == excludedMethodName) {
                excludedMethodName = ToolsKit.buildExcludedMethodName();
            }
            List<Class<?>> controllerClassList = ClassHelper.duang().getControllerClassList();
            if (ToolsKit.isEmpty(controllerClassList)) {
                LOG.info("Controller类为空,退出routeController方法");
                return;
            }
            for (Class<?> controllerClass : controllerClassList) {
                Method[] controllerMethodArray = controllerClass.getMethods();
                List<Method> methodList = new ArrayList<>();
                for (Method method : controllerMethodArray) {
                    if (!ToolsKit.isPublicMethod(method.getModifiers()) ||
                            excludedMethodName.contains(method.getName())) {
                        continue;
                    }
                    methodList.add(method);
                }
                if (ToolsKit.isNotEmpty(methodList)) {
                    Controller controllerAnnoy = controllerClass.getAnnotation(Controller.class);
                    String key = controllerAnnoy.value();
                    if (ToolsKit.isEmpty(key)) {
                        int endIndex = controllerClass.getSimpleName().toLowerCase().indexOf("controller");
                        if (endIndex > -1) {
                            key = controllerClass.getSimpleName().substring(0, endIndex);
                        }
                    }
                    Map<String, Method> methodMap = new HashMap<>();
                    for (Method method : methodList) {
                        methodMap.put(method.getName().toLowerCase(), method);
                    }
                    Route route = new Route(controllerClass, methodMap);
                    key = key.toLowerCase();
                    CONTROLLER_ROUTE_MAP.put(key, route);
                    BeanHandler.duang().setBean(route.getServiceObj());
                    ControllerFactory.setController(key, (IController) route.getServiceObj());
                }
            }
        }
    }

    private void routeService() {
        SERVICE_ROUTE_MAP.clear();
        if (null == excludedMethodName) {
            excludedMethodName = ToolsKit.buildExcludedMethodName();
        }
        List<Class<?>> serviceClassList = ClassHelper.duang().getServiceClassList();
        if (ToolsKit.isEmpty(serviceClassList)) {
            LOG.info("业务逻辑处理类为空,退出routeService方法");
            return;
        }
        for (Class<?> serviceClass : serviceClassList) {
            Method[] serviceMethodArray = serviceClass.getMethods();
            List<Method> methodList = new ArrayList<>();
            for (Method method : serviceMethodArray) {
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
                Route route = new Route(serviceClass, methodMap);
                SERVICE_ROUTE_MAP.put(key, route);
                BeanHandler.duang().setBean(route.getServiceObj());
            }
        }
        printRouteKey();
    }

    private void routeAction() {
        if (ACTION_ROUTE_MAP.isEmpty()) {
            List<Class<?>> actionClassList = ClassHelper.duang().getActionClassList();
            if (ToolsKit.isEmpty(actionClassList)) {
                LOG.info("工站逻辑处理类为空,退出routeAction方法");
                return;
            }
            for (Class<?> actionClass : actionClassList) {
                Action actionAonn = actionClass.getAnnotation(Action.class);
                if (ToolsKit.isEmpty(actionAonn)) {
                    continue;
                }
                IAction action = (IAction) ReflectUtil.newInstance(actionClass);
                if (ToolsKit.isNotEmpty(action)) {
                    String key = action.actionKey();
                    Route route = new Route(key, action);
                    ACTION_ROUTE_MAP.put(key, route);
                    BeanHandler.duang().setBean(route.getServiceObj());
                }
            }
            printActionKey();
        }
    }


    public static Map<String, Route> getRoutes() {
        Map<String,Route> routeMap = new HashMap<>();
        routeMap.putAll(CONTROLLER_ROUTE_MAP);
        routeMap.putAll(SERVICE_ROUTE_MAP);
        return routeMap;
    }

    private void printRouteKey() {

        if (!CONTROLLER_ROUTE_MAP.isEmpty()) {
            LOG.warn("**************** Controller Mapping ****************");
            List<String> keyList = new ArrayList<>(CONTROLLER_ROUTE_MAP.keySet());
            Collections.sort(keyList);
            for (String key : keyList) {
                Route route = CONTROLLER_ROUTE_MAP.get(key);
                for(Iterator<String> iterator = route.getMethodMap().keySet().iterator(); iterator.hasNext();) {
                    String methodName = iterator.next();
                    LOG.info(String.format("controller mapping: /%s/%s, route: %s", key, methodName, route.getServiceClass().getName()));
                }
            }
        }

        List<String> keyList = new ArrayList<>(SERVICE_ROUTE_MAP.keySet());
        if (keyList.isEmpty()) {
            throw new NullPointerException("业务逻辑处理类不存在！");
        }
        Collections.sort(keyList);
        LOG.warn("**************** Service Mapping ****************");
        for (String key : keyList) {
            Route route = SERVICE_ROUTE_MAP.get(key);
            LOG.info(String.format("service mapping: %s, route: %s", key, route.getServiceClass().getName()));
        }
    }

    private void printActionKey() {
        List<String> keyList = new ArrayList<>(ACTION_ROUTE_MAP.keySet());
        if (keyList.isEmpty()) {
            LOG.info("工站动作处理类不存在！");
            return;
        }
        Collections.sort(keyList);
        LOG.warn("**************** Action Mapping ****************");
        for (String key : keyList) {
            Route action = ACTION_ROUTE_MAP.get(key);
            LOG.info(String.format("action mapping: %s, action class: %s", key, action.getServiceClass().getName()));
        }
    }
}
