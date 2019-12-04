package com.openagv.opentcs.telegrams;

import cn.hutool.core.util.ObjectUtil;
import com.openagv.core.interfaces.IResponse;
import com.openagv.dto.PathStepDto;
import com.openagv.tools.ToolsKit;
import io.netty.handler.codec.http.HttpResponseStatus;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by laotang on 2019/9/26.
 */
public class Response implements IResponse {

    private String requestId;
    private Map<String,Object> params;
    private int status;
    private String cmdKey;
    private Object returnObj;
    private String deviceId;
    private String direction;
    private boolean isHandshakeList;

    public Response() {

    }

    private Response(String requestId) {
        this.params = new HashMap<>();
        this.requestId = requestId;
        status = HttpResponseStatus.INTERNAL_SERVER_ERROR.code();
        this.returnObj = null;
    }

    public static Response build(String requestId) {
        return new Response(requestId);
    }

    @Override
    public String getRequestId() {
        return requestId;
    }

    @Override
    public void setRequestId(String requestId) {
        this.requestId = requestId;
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
    public void setParams(String key, String value) {
        this.params.put(key, value);
    }

    @Override
    public void setStatus(int status) {
        this.status= status;
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
            return "hello,laotang";
        }
    }

    @Override
    public void setNextPointNames(List<String> pointNameList) {
        params.put(IResponse.TARGET_POINT_NAME, pointNameList);
    }

    @Override
    public List<String> getNextPointNames() {
        Object obj =  params.get(IResponse.TARGET_POINT_NAME);
        return ToolsKit.isEmpty(obj) ? null :  (List<String>)obj;
    }

    @Override
    public void setCmdKey(String key) {
        this.cmdKey =  String.valueOf(key);
    }

    @Override
    public String getCmdKey() {
        return cmdKey;
    }

    @Override
    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }
    @Override
    public String getDeviceId() {
        return deviceId;
    }


   public  void setDirection(String direction) {
        this.direction = direction;
    }

    public  String  getDirection() {
        return direction;
    }

    @Override
    public Map<String, Object> getParams() {
        return params;
    }

    @Override
    public void setHandshakeKey(String key) {
        params.put(IResponse.HANDSHAKE_NAME, key);
    }

    @Override
    public String getHandshakeKey() {
        return String.valueOf(params.get(IResponse.HANDSHAKE_NAME));
    }

    @Override
    public void setHandshakeListener(boolean isHandshakeList) {
        this.isHandshakeList = isHandshakeList;
    }

    @Override
    public boolean isHandshakeList() {
        return isHandshakeList;
    }

    /**
     * 设置参数步骤集合，主要用于路径运行一半后，出现故障或其它原因停止修复，修复后需要没执行的参数重发至客户端
     * @param stepList
     */
    @Override
    public void setPathStepList(List<PathStepDto> stepList) {
        java.util.Objects.requireNonNull(stepList, "步骤集合不能为空");
        getParams().put(PARAM_POINT_STEP, stepList);
    }
}
