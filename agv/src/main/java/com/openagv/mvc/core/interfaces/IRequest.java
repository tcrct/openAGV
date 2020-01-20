package com.openagv.mvc.core.interfaces;

import com.openagv.adapter.AgvCommAdapter;
import com.openagv.mvc.core.enums.ReqType;

/**
 * 请求对象接口
 * Created by laotang on 2020/1/12.
 */
public interface IRequest {

    /**
     * 取适配器
     * @return
     */
    AgvCommAdapter getAdapter();

    /**
     * 设置请求ID
     * @param id
     */
    void setId(String id);

    /**取请求ID*/
    String getId();

    /**
     * 设置协议对象
     * @param procolo
     */
    void setProtocol(IProtocol procolo);

    /**取出协议对象*/
    IProtocol getProtocol();

    /**
     * 请求类型枚举
     * @param reqType
     * */
     void setReqType(ReqType reqType);

    /**请求类型枚举*/
    ReqType getReqType();

    /**
     * 取原始协议字符串
     * @return
     */
    String getRawContent();

    /**
     * 设置原始协议字符串
     * @param raw 原始协议字符串
     * @return
     */
    void setRawContent(String raw);

}
