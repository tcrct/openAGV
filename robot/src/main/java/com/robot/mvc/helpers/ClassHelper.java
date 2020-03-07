package com.robot.mvc.helpers;

import cn.hutool.core.lang.Filter;
import cn.hutool.core.util.ClassUtil;
import com.robot.mvc.core.enums.AnnotationType;
import com.robot.utils.SettingUtil;
import com.robot.utils.ToolsKit;

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

    private boolean isScanClass(Class<?> clazz) {
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
                if (ToolsKit.isEmpty(tmpList)) {
                    CLASS_MAP.put(key, new ArrayList<Class<?>>() {
                        {
                            this.add(clazz);
                        }
                    });
                } else {
                    tmpList.add(clazz);
                }
                break;
            }
        }
    }

    /**
     *  取出所有业务类代码，一般用于热替换(热部署)功能
     *  不包括jar包下的类，仅包括classes文件下的所有class文件，一般是业务代码class
     *
     * @param classList       包路径，在该路径下的所有Class会扫描
     * @return
     */
    public void reSetAllBizClass(List<Class<?>> classList) {
        // 取出所有业务类之前，先将原有的
        CLASS_MAP.clear();
        // 将业务类按枚举名称作key，分类存放到CLASS_MAP中
        setClass2Map(classList);
    }

    private void setClass2Map(List<Class<?>> clazzList) {
        for(Class<?> clazz : clazzList) {
            binder(clazz);
        }
    }

    public List<Class<?>> getServiceClassList() {
        return CLASS_MAP.get(AnnotationType.SERVICE_ANNOTATION.getName());
    }

    public List<Class<?>> getControllerClassList() {
        return CLASS_MAP.get(AnnotationType.CONTROLLER_ANNOTATION.getName());
    }

    public List<Class<?>> getActionClassList() {
        return CLASS_MAP.get(AnnotationType.ACTION_ANNOTATION.getName());
    }

    public List<Class<?>> getEntityClassList() {
        return CLASS_MAP.get(AnnotationType.ENTITY_ANNOTATION.getName());
    }

    public List<Class<?>> getJobClassList() {
        return CLASS_MAP.get(AnnotationType.JOB_ANNOTATION.getName());
    }


    public List<Class<?>> getClassList(String key) {
        return CLASS_MAP.get(key);
    }

}
