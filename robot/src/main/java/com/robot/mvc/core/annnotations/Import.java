package com.robot.mvc.core.annnotations;

import java.lang.annotation.*;

/**
 * 依赖注入注解
 *
 * @author Laotang
 * @date 2020/2/2
 * @since 1.0
 */
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Import {

    String value() default "";

    String client() default "";

}
