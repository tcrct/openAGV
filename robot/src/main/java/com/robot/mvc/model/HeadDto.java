package com.robot.mvc.model;

import com.robot.utils.ToolsKit;

import java.util.HashMap;
import java.util.Map;

public class HeadDto implements java.io.Serializable {
    /**
     * 非0值均代表有异常
     */
    private int code = 0;
    /**
     * 异常信息
     */
    private String message = "success";

    /**
     * 客户端IP
     */
    private String clientIp;
    /**
     * 请求ID
     */
    private String requestId;

    /**
     * 请求URI
     */
    private String uri;
    /**
     * 响应返回时间
     */
    private String timestamp = ToolsKit.getCurrentDateString();

    /**
     * 请求头
     */
    private Map<String, String> headerMap = new HashMap<>();

    public HeadDto() {
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getClientIp() {
        return clientIp;
    }

    public void setClientIp(String clientIp) {
        this.clientIp = clientIp;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }


    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public Map<String, String> getHeaderMap() {
        return headerMap;
    }

    public void setHeaderMap(Map<String, String> headerMap) {
        this.headerMap = headerMap;
    }
}
