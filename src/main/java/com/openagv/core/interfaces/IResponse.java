package com.openagv.core.interfaces;

import java.util.Map;

/**
 * Created by laotang on 2019/9/25.
 */
public interface IResponse extends java.io.Serializable{

    String TARGET_POINT_NAME = "openAGV_TPN";

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
     * 添加返回扩展信息
     * @param key           名称
     * @param value         值
     */
    void setParams(String key, String value);

    /**
     * 取返回扩展信息
     * @return
     */
    Map<String,Object> getParams();

    /**
     * 设置返回状态标识
     * @return
     */
    void setStatus(int status);

    /**
     * 返回的内容
     * @return
     */
    String toString();

    /**
     * 设置目标位置点名称
     * @return
     */
    void setTargetPointName(String pointName);

    /**
     * 取目标位置点名称
     * @return
     */
    String getTargetPointName();

    /**
     * 设置请求指令
     * @param key
     */
    void setCmdKey(String key);

    /**
     * 取请求指令
     * @return
     */
    String getCmdKey();

}
