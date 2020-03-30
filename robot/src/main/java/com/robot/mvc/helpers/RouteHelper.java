package com.robot.mvc.helpers;

import cn.hutool.core.util.ReflectUtil;
import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import com.robot.config.Application;
import com.robot.event.core.EventListener;
import com.robot.mvc.core.annnotations.Action;
import com.robot.mvc.core.annnotations.Listener;
import com.robot.mvc.core.annnotations.Mapping;
import com.robot.mvc.core.interfaces.IAction;
import com.robot.mvc.model.Route;
import com.robot.utils.ToolsKit;
import org.opentcs.kernel.extensions.servicewebapi.console.SparkMappingFactory;
import org.opentcs.kernel.extensions.servicewebapi.console.interfaces.IController;

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
    /**
     * Listener类Map集合，key为监听器标识符，如果没有设置则为类全名
     */
    private static Map<String, Route> LISTENER_ROUTE_MAP = new HashMap<>();

    public static Map<String, Route> getControllerRouteMap() {
        return CONTROLLER_ROUTE_MAP;
    }

    public static Map<String, Route> getServiceRouteMap() {
        return SERVICE_ROUTE_MAP;
    }

    public static Map<String, Route> getActionRouteMap() {
        return ACTION_ROUTE_MAP;
    }

    public static Map<String, Route> getListenerRouteMap() {
        return LISTENER_ROUTE_MAP;
    }

    private static RouteHelper ROUTE_HELPER = null;

    private static final String PROTOCOL_SERVICE_NAME_FIELD = "ProtocolService";

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
      init();
    }

    private void init() {
        routeController();
        routeService();
        routeAction();
        routeListener();
    }


    private void routeController() {
        if (CONTROLLER_ROUTE_MAP.isEmpty()) {
            if (null == excludedMethodName) {
                excludedMethodName = ToolsKit.buildExcludedMethodName();
                // init方法不能对外映射
                excludedMethodName.add("init");
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
                    Mapping controllerMapping = controllerClass.getAnnotation(Mapping.class);
                    String key = null !=controllerMapping ? controllerMapping.value() : null;
                    if (ToolsKit.isEmpty(key)) {
                        int endIndex = controllerClass.getSimpleName().toLowerCase().indexOf("controller");
                        if (endIndex > -1) {
                            key = "/" + controllerClass.getSimpleName().substring(0, endIndex);
                        }
                    }
                    Map<String, Method> methodMap = new HashMap<>();
                    for (Method method : methodList) {
                        Mapping methodMapping = method.getClass().getAnnotation(Mapping.class);
                        String methodKey = null != methodMapping ? methodMapping.value() :
                                ("/".equals(key) ? method.getName() : "/" + method.getName());
                        methodMap.put(methodKey.toLowerCase(), method);
                    }
                    Route route = new Route(controllerClass, methodMap);
                    key = key.toLowerCase();
                    CONTROLLER_ROUTE_MAP.put(key, route);
                    BeanHelper.duang().setBean(route.getServiceObj());
                    SparkMappingFactory.setController(key, (IController) route.getServiceObj());
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
                Mapping serviceMapping = serviceClass.getAnnotation(Mapping.class);
                String key = null != serviceMapping ? serviceMapping.value() : null;
                if (ToolsKit.isEmpty(key)) {
                    int endIndex = serviceClass.getSimpleName().toLowerCase().indexOf("service");
                    if (endIndex > -1) {
                        // 继承了ProtocolService(车辆业务逻辑处理类)，只需要取XXXService的XXX作为key
                        if (PROTOCOL_SERVICE_NAME_FIELD.equals(serviceClass.getSuperclass().getSimpleName())) {
                            key = serviceClass.getSimpleName().substring(0, endIndex);
                        } else {
                            // 如果不是则取类的全名作为唯一标识
                            key = serviceClass.getName();
                        }
                    }
                }
                Map<String, Method> methodMap = new HashMap<>();
                for (Method method : methodList) {
                    methodMap.put(method.getName().toLowerCase(), method);
                }
                Route route = new Route(serviceClass, methodMap);
                SERVICE_ROUTE_MAP.put(key, route);
                BeanHelper.duang().setBean(route.getServiceObj());
            }
        }
        printRouteKey();
    }

    private void routeAction() {
        if (isRestart()) {
            return;
        }
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
                    BeanHelper.duang().setBean(route.getServiceObj());
                }
            }
            printActionKey();
        }
    }


    private void routeListener() {
        if (LISTENER_ROUTE_MAP.isEmpty()) {
            List<Class<?>> listenerClassList = ClassHelper.duang().getListenerClassList();
            if (ToolsKit.isEmpty(listenerClassList)) {
                LOG.info("监听器类为空,退出routeListener方法");
                return;
            }
            for (Class<?> listenerClass : listenerClassList) {
                Listener listenerAnnot = listenerClass.getAnnotation(Listener.class);
                if (ToolsKit.isEmpty(listenerAnnot)) {
                    continue;
                }
                EventListener eventListener = (EventListener) ReflectUtil.newInstance(listenerClass);
                if (ToolsKit.isNotEmpty(eventListener)) {
                    String key = listenerAnnot.key();
                    if (ToolsKit.isEmpty(key)) {
                        key = listenerClass.getName();
                    }
                    Route route = new Route(key, eventListener);
                    LISTENER_ROUTE_MAP.put(key, route);
                    BeanHelper.duang().setBean(route.getServiceObj());
                }
            }
            printListenetKey();
        }
    }

    private static final Map<String, Route> ALL_ROUTE_MAP = new HashMap<>();
    public static Map<String, Route> getRoutes() {
        if (ALL_ROUTE_MAP.isEmpty()) {
            ALL_ROUTE_MAP.putAll(CONTROLLER_ROUTE_MAP);
            ALL_ROUTE_MAP.putAll(SERVICE_ROUTE_MAP);
        }
        return ALL_ROUTE_MAP;
    }

    public static Route getServiceRoute(Class clazz) {
        String key = clazz.getName();
        if (PROTOCOL_SERVICE_NAME_FIELD.equals(clazz.getSuperclass().getSimpleName())) {
            int endIndex = clazz.getSimpleName().toLowerCase().indexOf("service");
            key = clazz.getSimpleName().substring(0, endIndex);
        }
        return SERVICE_ROUTE_MAP.get(key);
    }

    private void printRouteKey() {
        if (isRestart()) {
            return;
        }
        if (!CONTROLLER_ROUTE_MAP.isEmpty()) {
            LOG.warn("**************** Controller Mapping ****************");
            List<String> keyList = new ArrayList<>(CONTROLLER_ROUTE_MAP.keySet());
            Collections.sort(keyList);
            for (String controllerKey : keyList) {
                Route route = CONTROLLER_ROUTE_MAP.get(controllerKey);
                for(Iterator<String> iterator = route.getMethodMap().keySet().iterator(); iterator.hasNext();) {
                    String methodKey = iterator.next();
                    LOG.info(String.format("controller mapping: %s%s, route: %s", controllerKey, methodKey, route.getServiceClass().getName()));
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
        if (isRestart()) {
            return;
        }
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

    private void printListenetKey() {
        if (LISTENER_ROUTE_MAP.isEmpty()) {
            return;
        }
        List<String> keyList = new ArrayList<>(LISTENER_ROUTE_MAP.keySet());
        if (keyList.isEmpty()) {
            LOG.info("工站动作处理类不存在！");
            return;
        }
        Collections.sort(keyList);
        LOG.warn("**************** Listener Mapping ****************");
        for (String key : keyList) {
            Route route = LISTENER_ROUTE_MAP.get(key);
            LOG.info(String.format("listener mapping: %s, class: %s", key, route.getServiceClass().getName()));
        }
    }

    public boolean isRestart() {
        return Application.duang().isStarted();
    }
    public void reset() {
        clearMap();
        init();
    }

    private void clearMap(){
        getControllerRouteMap().clear();
        getServiceRouteMap().clear();
        getActionRouteMap().clear();
        getListenerRouteMap().clear();
        ALL_ROUTE_MAP.clear();
    }
}
