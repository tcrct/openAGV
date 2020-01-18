package com.openagv.mvc.core.interfaces;

/**
 * Created by laotang on 2020/1/12.
 */
public interface IResponse {

    String getId();

    void setStatus(int status);

    int getStatus();

    void write(Object message);

    Exception getException();

    default boolean isResponseTo(IRequest request) {
        return getId().equals(request.getId());
    }
}
