package com.openagv.mvc.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by laotang on 2020/1/12.
 */
public class ToolsKit {

    private static final Logger LOG = LoggerFactory.getLogger(ToolsKit.class);
    private static final Set<String> EXCLUDED_METHOD_NAME = new HashSet<>();

    /***
     * 判断传入的对象是否为空
     *
     * @param obj
     *            待检查的对象
     * @return 返回的布尔值,为空或等于0时返回true
     */
    public static boolean isEmpty(Object obj) {
        return checkObjectIsEmpty(obj, true);
    }

    /***
     * 判断传入的对象是否不为空
     *
     * @param obj
     *            待检查的对象
     * @return 返回的布尔值,不为空或不等于0时返回true
     */
    public static boolean isNotEmpty(Object obj) {
        return checkObjectIsEmpty(obj, false);
    }

    @SuppressWarnings("rawtypes")
    private static boolean checkObjectIsEmpty(Object obj, boolean bool) {
        if (null == obj) {
            return bool;
        }
        else if (obj == "" || "".equals(obj) || "null".equalsIgnoreCase(String.valueOf(obj))) {
            return bool;
        }
        else if (obj instanceof Integer || obj instanceof Long || obj instanceof Double) {
            try {
                Double.parseDouble(obj + "");
            } catch (Exception e) {
                return bool;
            }
        } else if (obj instanceof String) {
            if (((String) obj).length() <= 0) {
                return bool;
            }
            if ("null".equalsIgnoreCase(obj+"")) {
                return bool;
            }
        } else if (obj instanceof Map) {
            if (((Map) obj).size() == 0) {
                return bool;
            }
        } else if (obj instanceof Collection) {
            if (((Collection) obj).size() == 0) {
                return bool;
            }
        } else if (obj instanceof Object[]) {
            if (((Object[]) obj).length == 0) {
                return bool;
            }
        }
        return !bool;
    }


    /**
     * 构建过滤方法名集合，默认包含Object类里公共方法
     * @param excludeMethodClass  如果有指定，则添加指定类下所有方法名
     *
     * @return
     */
    public static Set<String> buildExcludedMethodName(Class<?>... excludeMethodClass) {
        if(EXCLUDED_METHOD_NAME.isEmpty()) {
            Method[] objectMethods = Object.class.getDeclaredMethods();
            for (Method m : objectMethods) {
                EXCLUDED_METHOD_NAME.add(m.getName());
            }
        }
        Set<String> tmpExcludeMethodName = null;
        if(null != excludeMethodClass) {
            tmpExcludeMethodName = new HashSet<>();
            for (Class excludeClass : excludeMethodClass) {
                Method[] excludeMethods = excludeClass.getDeclaredMethods();
                if (null != excludeMethods) {
                    for (Method method : excludeMethods) {
                        tmpExcludeMethodName.add(method.getName());
                    }
                }
            }
            tmpExcludeMethodName.addAll(EXCLUDED_METHOD_NAME);
        }
        return (null == tmpExcludeMethodName) ? EXCLUDED_METHOD_NAME : tmpExcludeMethodName;
    }

    /**
     * 是否正常公用的API方法
     * 正常方法是指访问权限是public的且不是抽像，静态，接口，Final的方法
     * @param mod       Modifier的mod
     * @return
     */
    public static boolean isPublicMethod(int mod) {
        return !(Modifier.isAbstract(mod) || Modifier.isStatic(mod) || Modifier.isFinal(mod) || Modifier.isInterface(mod) || Modifier.isPrivate(mod) || Modifier.isProtected(mod));
    }
}
