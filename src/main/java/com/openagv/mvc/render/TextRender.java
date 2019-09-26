package com.openagv.mvc.render;

import cn.hutool.core.util.ObjectUtil;
import com.openagv.exceptions.AgvException;

public class TextRender extends Render {

    private static final long serialVersionUID = 4775148244778489992L;
    private String text;

    public TextRender(Object text) {
        this(text, TEXT_PLAIN);
    }

    public TextRender(Object text, String contentType) {
        this.text = (text instanceof String) ? String.valueOf(text) : ObjectUtil.toString(text);
        TEXT_PLAIN = contentType;
    }

    @Override
    public void render() {
        if(null == request || null == response){
            logger.warn("request or response is null");
            return;
        }
        setDefaultValue2Response(TEXT_PLAIN);
        try {
            response.write(text);
        } catch (Exception e) {
            throw new AgvException(e.getMessage(), e);
        }
    }
}
