package com.openagv.core.interfaces;

import java.util.Map;

/**
 * Created by laotang on 2019/9/25.
 */
public interface IRequest {

    String getRequestId();

    <T> T getParameter(String name);

    Map<String, Object> getParameterMap();

    <T> T asBean(Class<T> clazz);

}
