package com.openagv.core;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.json.JSON;
import cn.hutool.json.JSONObject;
import com.openagv.core.interfaces.IRequest;
import io.netty.handler.codec.http.multipart.DiskAttribute;
import io.netty.handler.codec.http.multipart.DiskFileUpload;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by laotang on 2019/9/25.
 */
public class AgvRequest implements IRequest {

    private String requestId;
    private Map<String, Object> paramMap = new HashMap<>();

    static {
        DiskFileUpload.deleteOnExitTemporaryFile = true;
        DiskFileUpload.baseDirectory = null;
        DiskAttribute.deleteOnExitTemporaryFile = true;
        DiskAttribute.baseDirectory = null;
    }

    private AgvRequest(Map<String,Object> paramMap) {
        requestId = IdUtil.objectId();
        if(null != paramMap) {
            this.paramMap.putAll(paramMap);
        }
    }

    public static AgvRequest build(Map<String,Object> paramMap) {
        return new AgvRequest(paramMap);
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
    public <T> T asBean(Class<T> clazz) {
        return BeanUtil.mapToBean(paramMap, clazz, true);
    }


}
