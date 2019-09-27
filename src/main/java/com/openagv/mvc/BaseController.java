package com.openagv.mvc;
import com.openagv.core.interfaces.IRequest;
import com.openagv.core.interfaces.IResponse;
import com.openagv.mvc.render.Render;
import com.openagv.mvc.render.TextRender;

public abstract class BaseController {

    private IRequest request;
    private IResponse response;
    private Render render;

    public IRequest getRequest() {
        return request;
    }

    public IResponse getResponse() {
        return response;
    }

    public void init(IRequest request, IResponse response) {
        this.request = request;
        this.response = response;
    }

    public Render getRender(Object resultObj) {
        if(null == render) {
            if(null != resultObj && (resultObj instanceof String)) {
                render = new TextRender(resultObj);
            } else {
                render = new TextRender("controller is not set render value");
            }
        }
        return render;
    }

}
