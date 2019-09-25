package com.openagv.core;

import com.openagv.core.interfaces.IRequest;
import com.openagv.core.interfaces.IResponse;

/**
 * Created by laotang on 2019/9/26.
 */
public class AgvResponse implements IResponse {

    private IRequest request;

    private AgvResponse(IRequest request) {
        this.request = request;
    }

    public static AgvResponse build(IRequest request) {
        return new AgvResponse(request);
    }

    @Override
    public String getRequestId() {
        return request.getRequestId();
    }

    @Override
    public IRequest getRequest() {
        return request;
    }

    @Override
    public void write(Object returnObj) {

    }

    @Override
    public String toString() {
        return null;
    }

}
