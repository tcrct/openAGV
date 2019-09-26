package com.openagv.core.interfaces;

/**
 * Created by laotang on 2019/9/25.
 */
public interface IResponse {

    /**
     * 取出请求对象
     * @return
     */
    IRequest getRequest();

    /**
     * 请求ID
     * @return 返回请求ID
     */
    String getRequestId();

    /**
     * 设置返回主体内容
     * @param returnObj     返回主体对象
     */
    void write(Object returnObj);

    /**
     * 取返回状态标识
     * @return
     */
    int getStatus();

    /**
     * 添加返回头信息
     * @param key           名称
     * @param value         值
     */
    void setHeader(String key, String value);

    /**
     * 设置返回状态标识
     * @return
     */
    void setStatus(int status);

    /**
     * 设置返回ContentType信息
     * @param contentType
     */
    void setContentType(String contentType);

    /**
     * 设置返回的编码格式
     * @param encoding
     */
    void setCharacterEncoding(String encoding);

    /**
     * 返回的内容
     * @return
     */
    String toString();

}
