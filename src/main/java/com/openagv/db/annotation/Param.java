package com.openagv.db.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;

/**
 * @author Created by laotang
 * @date createed in 2018/5/24.
 */
@Target({PARAMETER, TYPE, CONSTRUCTOR,LOCAL_VARIABLE, METHOD, FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Param {
    // 是否允许值为null或空字串符 默认为允许
    boolean isEmpty() default true;

    // 提交参数的名称
    String name() default "";

    // 参数默认值，如果没值会
    String defaultValue() default "";

    // 参数说明
    String desc() default "";

    // 参数类型
    Class<?> type() default Object.class;

    // 对自定义的javabean进行参数说明与验证，该class对象要存在@Vtor注解
    Class<?> bean() default Object.class;

    // 表单id值，例如：<input id="user_name" />
    String id() default "";

    // 表单显示字段值，例如: <input  label="姓名" />
    String label() default "";

    // 是否隐藏，默认为否 例如: <input  hidden="none" />
    boolean isHidden() default false;

}
