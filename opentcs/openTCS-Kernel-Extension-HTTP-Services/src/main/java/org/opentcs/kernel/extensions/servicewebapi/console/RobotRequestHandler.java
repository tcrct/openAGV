/**
 * Copyright (c) The openTCS Authors.
 * <p>
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel.extensions.servicewebapi.console;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.google.common.base.Strings;
import org.opentcs.components.kernel.services.VehicleService;
import org.opentcs.kernel.extensions.servicewebapi.HttpConstants;
import org.opentcs.kernel.extensions.servicewebapi.RequestHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Request;
import spark.Response;
import spark.Service;

import javax.inject.Inject;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.text.SimpleDateFormat;
import java.util.HashSet;
import java.util.Set;

import static java.util.Objects.requireNonNull;

/**
 * Handles requests and produces responses for version 1 of the web API.
 *
 * @author Laotang
 * @blame Robot
 */
public class RobotRequestHandler
        implements RequestHandler {

    private static Logger logger = LoggerFactory.getLogger(RobotRequestHandler.class);

    /**
     * Whether this instance is initialized.
     */
    private boolean initialized;
    private VehicleService vehicleService;

    private static ObjectMapper objectMapper = new ObjectMapper();

    static {
        /**过滤对象的null属性*/
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        /**过滤map中的null key*/
        objectMapper.getSerializerProvider().setNullKeySerializer(new JsonSerializer<Object>() {
            @Override
            public void serialize(Object value, JsonGenerator generator, SerializerProvider serializers) throws IOException, JsonProcessingException {
                generator.writeFieldName("");
            }
        });
        /**过滤map中的null值*/
        objectMapper.getSerializerProvider().setNullValueSerializer(new JsonSerializer<Object>() {
            @Override
            public void serialize(Object value, JsonGenerator generator, SerializerProvider serializers) throws IOException, JsonProcessingException {
                generator.writeString("");
            }
        });
        //指定遇到date按照这种格式转换
        objectMapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
    }


    @Inject
    public RobotRequestHandler(VehicleService vehicleService) {
        this.vehicleService = vehicleService;
    }

    @Override
    public void initialize() {
        buildExcludedMethodName();
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
        logger.info("RobotRequestHandler 销毁成功");
    }

    // TODO 控制中心(web) api路由
    @Override
    public void addRoutes(Service service) {
        requireNonNull(service, "service");
        //将所有[get/post]请求转向到指定的类进行处理
        service.get("/*", this::toControllerMethod);
        service.post("/*", this::toControllerMethod);
    }

    private Object toControllerMethod(Request request, Response response)
            throws IllegalArgumentException, IllegalStateException {
        String uri = request.uri().toLowerCase();
        if ("/".equals(uri)) {
            return  "please set access path, not supported '/' access system!";
        }
        String[] uriArray = uri.split("/");
        String controllerName = uriArray[1];
        IController controller = ControllerFactory.getController(controllerName);
        if (null == controller) {
            logger.error("根据[{}]找不到对应的Controller，请检查Controller映射路径规则是否符合规则！", controllerName);
            logger.error(" \n 规则如下：\n uri第一层目录结构必须是与是以该目录名称命名的Controller，如: /user/add，则Controller文件应该为UserController.或在Controller Mapping指定");
            return "access is not allowed";
        }
        try {
            Method method = getMethod(controller.getClass(), uri, controllerName); //METHOD_MAP.get(request.uri().toLowerCase());
            if (null == method) {
                throw new NullPointerException("根据[" + request.uri() + "]查找不到对应的Controller Method，请检查路径是否正确！");
            }
            response.type(HttpConstants.CONTENT_TYPE_APPLICATION_JSON_UTF8);
//            request.attribute("_vehicleService", vehicleService);
            controller.init(request, response);
            Object resuleObj = method.invoke(controller);
            if (!(resuleObj instanceof String)) {
                resuleObj = toJson(resuleObj);
            }
            return resuleObj;
        } catch (Exception e) {
            e.printStackTrace();
            return e;
        }
    }

    private Method getMethod(Class<?> controllerClass, String uri, String controllerName){
        if (null == controllerName) {
            throw new NullPointerException("controller name is empty");
        }
        Method method = ControllerFactory.getMethodMap().get(uri);
        if (null != method) {
            return method;
        } else {
            try {
//                consoleControllerClass = Class.forName(System.getProperty(ID));
//                Injector injector = Guice.createInjector(new Module() {
//                    @Override
//                    public void configure(Binder binder) {
//                        binder.bind(consoleControllerClass).in(Scopes.SINGLETON);
//                    }
//                });
//                consoleControllerObj = injector.getInstance(consoleControllerClass);

                // 根据请求的URI与method对应的Mapping关联
                Method[] methods = controllerClass.getMethods();
                for (Method action : methods) {
                    if (!isPublicMethod(action.getModifiers()) ||
                            EXCLUDED_METHOD_NAME.contains(action.getName())) {
                        continue;
                    }
                    String key = action.getName();
                    Annotation[] annotations = action.getAnnotations();
                    if (null != annotations && annotations.length >= 1) {
                        for (Annotation annotation : annotations) {
                            String annotString = annotation.toString();
                            if (!Strings.isNullOrEmpty(annotString) && annotString.contains("Mapping")) {
                                annotString = annotString.substring(annotString.indexOf("(") + 1, annotString.indexOf(")"));
                                String[] annStringArray = annotString.split(",");
                                for (String annItem : annStringArray) {
                                    String[] annItemArray = annItem.split("=");
                                    if ("value".equalsIgnoreCase(annItemArray[0])) {
                                        // 以注解的value值为映射路径
                                        key = annItemArray[1].trim().toLowerCase();
                                        key = "/" + controllerName + (key.startsWith("/")?key:"/" + key);
                                        ControllerFactory.getMethodMap().put(key, action);
                                    }
                                }
                            }
                        }
                    } else {
                        ControllerFactory.getMethodMap().put("/" + controllerName + "/" + key.toLowerCase(), action);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return ControllerFactory.getMethodMap().get(uri);
    }

    private String toJson(Object object) throws IllegalStateException {
        try {
            return objectMapper
                    .writerWithDefaultPrettyPrinter()
                    .writeValueAsString(object);
        }
        catch (JsonProcessingException exc) {
            throw new IllegalStateException("Could not produce JSON output", exc);
        }
    }

    private boolean isPublicMethod(int mod) {
        return !(Modifier.isAbstract(mod) || Modifier.isStatic(mod) || Modifier.isFinal(mod) || Modifier.isInterface(mod) || Modifier.isPrivate(mod) || Modifier.isProtected(mod));
    }

    private static final Set<String> EXCLUDED_METHOD_NAME = new HashSet<>();
    private void buildExcludedMethodName() {
        if (EXCLUDED_METHOD_NAME.isEmpty()) {
            Method[] objectMethods = Object.class.getDeclaredMethods();
            for (Method m : objectMethods) {
                EXCLUDED_METHOD_NAME.add(m.getName());
            }
        }
    }
}
