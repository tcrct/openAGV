package com.robot.mvc.core.annnotations;

import java.lang.annotation.*;

/**
 * Controller类注解
 * 所有的Controller都要注明该注解，用于系统启动时扫描注册
 *
 * @author Laotang
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Controller {
    String value() default "";
}
