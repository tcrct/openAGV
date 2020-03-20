package com.robot.mvc.core.annnotations;

import java.lang.annotation.*;

/**
 *  Controller类的映射注解
 * 自定义映射路径
 *
 * @author Laotang
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Mapping {
    /**路径值*/
    String value() default "";
    /**方法说明*/
    String desc() default "";
}
