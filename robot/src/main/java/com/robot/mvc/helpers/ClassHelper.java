package com.robot.mvc.helpers;

import cn.hutool.core.lang.Filter;
import cn.hutool.core.util.ClassUtil;
import com.robot.mvc.core.enums.AnnotationType;
import com.robot.mvc.utils.SettingUtil;
import com.robot.mvc.utils.ToolsKit;

import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 类帮助器
 */
public class ClassHelper {

    /**
     * 所有扫描类
     */
    private static final Map<String, List<Class<?>>> CLASS_MAP = new HashMap<>();
    private final static Lock lock = new ReentrantLock();

    private static ClassHelper CLASS_HELPER = null;
    public static ClassHelper duang() {
        try {
            lock.lock();
            if (null == CLASS_HELPER) {
                CLASS_HELPER = new ClassHelper();
            }
        } finally {
            lock.unlock();
        }
        return CLASS_HELPER;
    }

    private ClassHelper() {
        String packageName = SettingUtil.getString("package.name");
        Set<Class<?>> classSet = ClassUtil.scanPackage(packageName, new Filter<Class<?>>() {
            @Override
            public boolean accept(Class<?> aClass) {
                return isScanClass(aClass);
            }
        });
        for (Class clazz : classSet) {
            binder(clazz);
        }
    }

    private static boolean isScanClass(Class<?> clazz) {
        if (null != clazz) {
            for (AnnotationType annotationEnum : AnnotationType.values()) {
                if (clazz.isAnnotationPresent(annotationEnum.getClazz())) {
                    return true;
                }
            }
        }
        return false;
    }

    private void binder(Class clazz) {
        for (AnnotationType annotationEnum : AnnotationType.values()) {
            if (clazz.isAnnotationPresent(annotationEnum.getClazz())) {
                String key = annotationEnum.getName();
                List<Class<?>> tmpList = CLASS_MAP.get(key);
                if(ToolsKit.isEmpty(tmpList)) {
                    CLASS_MAP.put(key, new ArrayList<Class<?>>(){ { this.add(clazz);} });
                } else {
                    tmpList.add(clazz);
                }
                break;
            }
        }
    }

    public List<Class<?>> getServiceClassList() {
        return CLASS_MAP.get(AnnotationType.SERVICE_ANNOTATION.getName());
    }

    public List<Class<?>> getControllerClassList() {
//        return CLASS_MAP.get(AnnotationType.CONTROLLER_ANNOTATION.getName());
        return null;
    }

    public List<Class<?>> getActionClassList() {
        return CLASS_MAP.get(AnnotationType.ACTION_ANNOTATION.getName());
    }

    public List<Class<?>> getEntityClassList() {
//        return CLASS_MAP.get(AnnotationType.ENTITY_ANNOTATION.getName());
        return null;
    }

    public List<Class<?>> getJobClassList() {
        return CLASS_MAP.get(AnnotationType.JOB_ANNOTATION.getName());
    }


    public List<Class<?>> getClassList(String key) {
        return CLASS_MAP.get(key);
    }

}
