package com.openagv.mvc;

import cn.hutool.core.util.ObjectUtil;
import com.openagv.core.interfaces.IRequest;
import com.openagv.core.interfaces.IResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpResponseStatus;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by laotang on 2019/9/26.
 */
public class Response implements IResponse {

    private IRequest request;
    private Map<String,String> headers;
    private int status;
    private String charset;
    private Object returnObj;

    private Response(IRequest request) {
        this.headers = new HashMap<>();
        this.request = request;
        status = HttpResponseStatus.OK.code();
        this.returnObj = null;
    }

    public static Response build(IRequest request) {
        return new Response(request);
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
        this.returnObj = returnObj;
    }

    @Override
    public int getStatus() {
        return status;
    }

    @Override
    public void setHeader(String key, String value) {
        this.headers.put(key, value);
    }

    @Override
    public void setStatus(int status) {
        this.status= status;
    }

    @Override
    public void setContentType(String contentType) {
        headers.put(HttpHeaderNames.CONTENT_TYPE.toString(), contentType);
    }

    @Override
    public void setCharacterEncoding(String encoding) {
        this.charset = encoding;
        headers.put(HttpHeaderNames.ACCEPT_ENCODING.toString(), encoding);
        headers.put(HttpHeaderNames.CONTENT_ENCODING.toString(), encoding);
        headers.put(HttpHeaderNames.CONTENT_TRANSFER_ENCODING.toString(), encoding);
        headers.put(HttpHeaderNames.TRANSFER_ENCODING.toString(), encoding);
    }

    @Override
    public String toString() {
        if(null != returnObj) {
            if(returnObj instanceof String) {
                return (String)returnObj;
            } else {
                return ObjectUtil.toString(returnObj);
            }
        } else{
            return "Hello, Laotang";
        }
    }

}
