package com.openagv.mvc;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.IdUtil;
import com.openagv.core.interfaces.IRequest;
import com.openagv.tools.ToolsKit;
import io.netty.handler.codec.http.multipart.DiskAttribute;
import io.netty.handler.codec.http.multipart.DiskFileUpload;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by laotang on 2019/9/25.
 */
public class Request implements IRequest {

    private String requestId;
    private Map<String, Object> paramMap = new HashMap<>();

    static {
        DiskFileUpload.deleteOnExitTemporaryFile = true;
        DiskFileUpload.baseDirectory = null;
        DiskAttribute.deleteOnExitTemporaryFile = true;
        DiskAttribute.baseDirectory = null;
    }

    private Request(Map<String,Object> paramMap) {
        requestId = IdUtil.objectId();
        if(null != paramMap) {
            this.paramMap.putAll(paramMap);
        }
    }

    public static Request build(Map<String,Object> paramMap) {
        return new Request(paramMap);
    }

    @Override
    public String getRequestId() {
        return requestId;
    }

    @Override
    public <T> T getParameter(String name) {
        return null;
    }

    @Override
    public Map<String, Object> getParameterMap() {
        return paramMap;
    }

    @Override
    public String getRequestURI() {
        Object uri = paramMap.get(TELEGRAM_TARGET);
        if(ToolsKit.isEmpty(uri)) {
            //
        }
        return String.valueOf(uri);
    }

    @Override
    public <T> T asBean(Class<T> clazz) {
        return BeanUtil.mapToBean(paramMap, clazz, true);
    }


}
