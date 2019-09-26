package com.openagv.core;

import com.openagv.core.interfaces.IRequest;
import com.openagv.core.interfaces.IResponse;

public class AgvResult implements java.io.Serializable{

    private IRequest request;
    private IResponse response;

    public AgvResult() {
    }

    public AgvResult(IRequest request, IResponse response) {
        this.request = request;
        this.response = response;
    }

    public IRequest getRequest() {
        return request;
    }

    public void setRequest(IRequest request) {
        this.request = request;
    }

    public IResponse getResponse() {
        return response;
    }

    public void setResponse(IResponse response) {
        this.response = response;
    }
}
