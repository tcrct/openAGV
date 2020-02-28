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
    String value() default "";
}
