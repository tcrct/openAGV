package com.robot.mvc.core.interfaces;

import com.robot.adapter.RobotCommAdapter;
import com.robot.mvc.core.enums.ReqType;

import java.util.Map;

/**
 * 请求对象接口
 * Created by laotang on 2020/1/12.
 */
public interface IRequest extends ITelegram {

    /**
     * 取适配器
     * @return
     */
    RobotCommAdapter getAdapter();

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

    /***
     * 请求参数值，用于扩展自定义参数
     * @return
     */
    Map<String, Object> getParams();

}
