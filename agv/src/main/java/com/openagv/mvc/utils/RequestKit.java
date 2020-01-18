package com.openagv.mvc.utils;

import com.openagv.mvc.core.interfaces.IRequest;
import com.openagv.mvc.core.interfaces.IResponse;
import com.openagv.mvc.core.telegram.MoveRequest;
import com.openagv.mvc.main.DispatchFactory;

public class RequestKit {

    private IRequest request;

    public static RequestKit duang() {
        return new RequestKit();
    }

    public RequestKit request(IRequest request) {
        this.request = request;
        return this;
    }

    public IResponse execute() throws Exception {
        if (request instanceof MoveRequest) {
//            DispatchFactory.dispatch(request);
        }
        return null;
    }

}
