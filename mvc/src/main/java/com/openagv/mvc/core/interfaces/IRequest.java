package com.openagv.mvc.core.interfaces;

import com.openagv.mvc.core.enums.ReqType;

/**
 * Created by laotang on 2020/1/12.
 */
public interface IRequest {

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


    String getRawContent();
}
