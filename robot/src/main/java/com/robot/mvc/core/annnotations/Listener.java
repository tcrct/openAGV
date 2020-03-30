package com.robot.mvc.core.annnotations;

import java.lang.annotation.*;

/**
 * Listener类注解
 *所有的Listener都要注明该注解，用于系统启动时扫描注册
 *
 *
 * @author Laotang
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Listener {
    // 该监听器的标识符
    String key() default "";
}
