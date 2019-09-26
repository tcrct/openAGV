package com.openagv.core.interfaces;

import java.util.Map;

/**
 * Created by laotang on 2019/9/25.
 */
public interface IRequest {

    String TELEGRAM_TARGET = "telegram_target";

    String getRequestId();

    <T> T getParameter(String name);

    Map<String, Object> getParameterMap();

    String getRequestURI();

    <T> T asBean(Class<T> clazz);

}
