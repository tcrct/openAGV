package com.openagv.core.helper;

import cn.hutool.core.lang.Filter;
import cn.hutool.core.util.ClassUtil;
import com.openagv.tools.SettingUtils;
import com.openagv.tools.ToolsKit;

import java.util.*;

public class ClassHelper {

    /**
     * 所有扫描类
     */
    private static final Map<String, List<Class<?>>> CLASS_MAP = new HashMap<>();

    private static ClassHelper classHelper = new ClassHelper();
    public static ClassHelper duang() {
        return classHelper;
    }

    private ClassHelper() {}

    public void init() {
        String packageName = SettingUtils.getString("package.name");
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
            for (AnnotationEnum annotationEnum : AnnotationEnum.values()) {
                if (clazz.isAnnotationPresent(annotationEnum.getClazz())) {
                    return true;
                }
            }
        }
        return false;
    }

    private void binder(Class clazz) {
        for (AnnotationEnum annotationEnum : AnnotationEnum.values()) {
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
        return CLASS_MAP.get(AnnotationEnum.SERVICE_ANNOTATION.getName());
    }

    public List<Class<?>> getControllerClassList() {
        return CLASS_MAP.get(AnnotationEnum.CONTROLLER_ANNOTATION.getName());
    }

    public List<Class<?>> getEntityClassList() {
        return CLASS_MAP.get(AnnotationEnum.ENTITY_ANNOTATION.getName());
    }

    public List<Class<?>> getClassList(String key) {
        return CLASS_MAP.get(key);
    }

}
