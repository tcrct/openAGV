/**
 * Copyright (c) The openTCS Authors.
 * <p>
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel.extensions.servicewebapi.console;

import com.google.inject.*;
import org.opentcs.components.kernel.services.VehicleService;
import org.opentcs.kernel.extensions.servicewebapi.HttpConstants;
import org.opentcs.kernel.extensions.servicewebapi.RequestHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Request;
import spark.Response;
import spark.Service;

import javax.inject.Inject;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import static java.util.Objects.requireNonNull;

/**
 * Handles requests and produces responses for version 1 of the web API.
 *
 * @author Laotang
 * @blame Robot
 */
public class ConsoleRequestHandler
        implements RequestHandler {

    private static Logger logger = LoggerFactory.getLogger(ConsoleRequestHandler.class);

    /**
     * Whether this instance is initialized.
     */
    private boolean initialized;

    // 实际处理的Controller
    private static final String CONTROLLER_CLASS_NAME = "com.robot.service.console.ConsoleController";

    private Class consoleControllerClass;
    private Object consoleControllerObj;
    private VehicleService vehicleService;
    private Map<String, Method> METHOD_MAP = new HashMap<>();

    @Inject
    public ConsoleRequestHandler(VehicleService vehicleService) {
        this.vehicleService = vehicleService;
        init();
    }

    private void init() {
        if (null == consoleControllerClass) {
            try {
                consoleControllerClass = Class.forName(CONTROLLER_CLASS_NAME);
                Injector injector = Guice.createInjector(new Module() {
                    @Override
                    public void configure(Binder binder) {
                        binder.bind(consoleControllerClass).in(Scopes.SINGLETON);
                    }
                });
                consoleControllerObj = injector.getInstance(consoleControllerClass);
                // 根据请求的URI与method对应的Mapping关联
                Method[] methods = consoleControllerObj.getClass().getMethods();
                for (Method method : methods) {
                    Annotation[] annotations = method.getAnnotations();
                    if (null != annotations && annotations.length >= 1) {
                        Annotation annotation = annotations[0];
                        String annotString = annotation.toString();
                        annotString = annotString.substring(annotString.indexOf("(") + 1, annotString.indexOf(")"));
                        String[] annStringArray = annotString.split(",");
                        for (String annItem : annStringArray) {
                            String[] annItemArray = annItem.split("=");
                            if ("value".equalsIgnoreCase(annItemArray[0])) {
                                METHOD_MAP.put(annItemArray[1], method);
                            }
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void initialize() {
        initialized = true;
        logger.info("ConsoleRequestHandler 初始化成功");
    }

    @Override
    public boolean isInitialized() {
        return initialized;
    }

    @Override
    public void terminate() {
        if (!isInitialized()) {
            return;
        }
        initialized = false;
        logger.info("ConsoleRequestHandler 销毁成功");
    }

    // TODO 控制中心(web) api路由
    @Override
    public void addRoutes(Service service) {
        requireNonNull(service, "service");
        //将所有请求转向到指定的类进行处理
        service.get("/*", this::toControllerMethod);
    }

    private Object toControllerMethod(Request request, Response response)
            throws IllegalArgumentException, IllegalStateException {
        response.type(HttpConstants.CONTENT_TYPE_APPLICATION_JSON_UTF8);
        request.attribute("AGV_VehicleService", vehicleService);
        try {
            Method method = METHOD_MAP.get(request.uri());
            if (null == method) {
                throw new NullPointerException("根据[" + request.uri() + "]查找不到对应的Method，请确保在["+CONTROLLER_CLASS_NAME+"]已经设置！");
            }
            return method.invoke(consoleControllerObj, request, response);
        } catch (Exception e) {
            e.printStackTrace();
            return e;
        }
    }

}
