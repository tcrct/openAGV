package com.openagv.mvc.core.annnotations;

import java.lang.annotation.*;

/**
 * Action类注解
 * 所有的Action都要注明该注解，用于系统启动时扫描注册
 *
 * @author Laotang
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Action {
    String name() default "";
}
