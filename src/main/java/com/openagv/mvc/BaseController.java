package com.openagv.mvc;
import com.openagv.mvc.render.Render;
import com.openagv.mvc.render.TextRender;

public abstract class BaseController {

    private Render render;

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
