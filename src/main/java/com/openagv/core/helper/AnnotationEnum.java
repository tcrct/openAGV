package com.openagv.core.helper;

import com.duangframework.db.annotation.Entity;
import com.openagv.core.annotations.Controller;
import com.openagv.core.annotations.Service;

import java.lang.annotation.Annotation;

public enum AnnotationEnum {
    CONTROLLER_ANNOTATION(Controller.class, true,"所有Controller类的注解，必须在类添加该注解否则框架忽略扫描"),
    SERVICE_ANNOTATION(Service.class, true,"所有Service类的注解，必须在类添加该注解否则框架忽略扫描"),
    ENTITY_ANNOTATION(Entity.class, false,"所有Entity类的注解，必须在类添加该注解否则框架忽略扫描"),
    ;

    Class<? extends Annotation> clazz;
    String name;
    // 是否需要实例化， true为需要
    boolean instance;
    String desc;

    private AnnotationEnum(Class<? extends Annotation> clazz, boolean instance, String desc) {
        this.clazz = clazz;
        this.instance = instance;
        this.desc = desc;
    }

    public Class<? extends Annotation> getClazz() {
        return clazz;
    }

    public String getName() {
        return clazz.getName();
    }

    /**
     * 是否需要实例化， true为需要
     * @return
     */
    public boolean getInstance() {
        return instance;
    }

    public String desc() {
        return desc;
    }
}
