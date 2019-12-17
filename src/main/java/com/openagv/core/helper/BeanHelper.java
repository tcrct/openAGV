//package com.openagv.core.helper;
//
//
//
//import com.openagv.core.annotations.Service;
//import com.openagv.tools.ToolsKit;
//
//import java.lang.reflect.Method;
//import java.util.*;
//
//public class BeanHelper {
//
//    private static BeanHelper beanHelper = new BeanHelper();
//    private static Set<String> excludedMethodName = null;
//
//    public static BeanHelper duang() {
//        if (null == excludedMethodName) {
//            excludedMethodName = ToolsKit.buildExcludedMethodName();
//        }
//        return beanHelper;
//    }
//
//
//
//    private static final Map<String,ServiceBean> STRING_SERVICE_BEAN_MAP = new HashMap<>();
//    public Map<String, ServiceBean> toServiceBean() {
//        if (STRING_SERVICE_BEAN_MAP.isEmpty()) {
//            List<Class<?>> serviceClassList = ClassHelper.duang().getServiceClassList();
//            for (Class<?> serviceClass : serviceClassList) {
//                Method[] methodArray = serviceClass.getMethods();
//                List<Method> methodList = new ArrayList<>();
//                for (Method method : methodArray) {
//                    if (!ToolsKit.isPublicMethod(method.getModifiers()) ||
//                            excludedMethodName.contains(method.getName())) {
//                        continue;
//                    }
//                    methodList.add(method);
//                }
//                if (ToolsKit.isNotEmpty(methodList)) {
//                    Service serviceAnnot = serviceClass.getAnnotation(Service.class);
//                    String key = serviceAnnot.value();
//                    if (ToolsKit.isEmpty(key)) {
//                        int endIndex = serviceClass.getSimpleName().toLowerCase().indexOf("service");
//                        if(endIndex > -1) {
//                            key = serviceClass.getSimpleName().substring(0, endIndex);
//                        }
//                    }
//                    Map<String, Method> methodMap = new HashMap<>();
//                    for (Method method : methodList) {
//                        methodMap.put(method.getName().toLowerCase(), method);
//                    }
//                    STRING_SERVICE_BEAN_MAP.put(key, new ServiceBean(serviceClass, methodMap));
//                }
//            }
//        }
//        return STRING_SERVICE_BEAN_MAP;
//    }
//
//
//}
