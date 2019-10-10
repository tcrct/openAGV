package com.openagv.mvc.render;

import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import com.openagv.core.interfaces.IRequest;
import com.openagv.core.interfaces.IResponse;
import io.netty.handler.codec.http.HttpConstants;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpResponseStatus;

import java.io.Serializable;

public abstract class Render implements Serializable {

    protected static final Log logger = LogFactory.get();

    protected static final String ENCODING  = HttpConstants.DEFAULT_CHARSET.toString();
    protected static String TEXT_PLAIN = HttpHeaderValues.TEXT_PLAIN.toString()+";charset=" + ENCODING;

    protected IRequest request;
    protected IResponse response;

    public final Render setContext(IRequest request, IResponse response) {
        this.request = request;
        this.response = response;
        return this;
    }


    protected void setDefaultValue2Response(String contentType) {
        response.setParams(HttpHeaderNames.PRAGMA.toString(), HttpHeaderValues.NO_CACHE.toString());
        response.setParams(HttpHeaderNames.CACHE_CONTROL.toString(), HttpHeaderValues.NO_CACHE.toString());
        response.setParams(HttpHeaderNames.EXPIRES.toString(), HttpHeaderValues.ZERO.toString());
        response.setParams(HttpHeaderNames.CONTENT_TYPE.toString(), contentType);
        response.setStatus((response.getStatus()==HttpResponseStatus.INTERNAL_SERVER_ERROR.code()) ? HttpResponseStatus.OK.code() : response.getStatus());
    }

    public abstract void render();


}
