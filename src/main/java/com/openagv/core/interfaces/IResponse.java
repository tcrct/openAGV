package com.openagv.core.interfaces;

/**
 * Created by laotang on 2019/9/25.
 */
public interface IResponse {

    IRequest getRequest();

    String getRequestId();

    void write(Object returnObj);

}
