package com.openagv.core.interfaces;

import com.openagv.opentcs.model.ProcessModel;
import org.opentcs.drivers.vehicle.MovementCommand;

import java.util.Map;

/**
 * Created by laotang on 2019/9/25.
 */
public interface IRequest {

    /**唯一的请求ID，用ObjectId作标识*/
    String getRequestId();

    /**根据名称取参数值*/
    <T> T getParameter(String name);

    /**参数值KV集合*/
    Map<String, Object> getParameterMap();

    /**请求路径*/
    String getRequestURI();

    /**设置请求目的路径*/
    void setTarget(String target);

    /**取报文的原始字符串*/
    String getOriginalTelegram();

    /**转换为Bean*/
    <T> T asBean(Class<T> clazz);

    /**发送车辆进程参数模型*/
    void setModel(ProcessModel processModel);

    /**取车辆进程参数模型*/
    ProcessModel getProcessModel();

    /**设置车辆移动命令*/
    void setCmd(MovementCommand cmd);

    /**取车辆移动命令*/
    MovementCommand getMovementCommand();

    /**取车辆移动命令*/
    Object getContentBean();

    /**设置*/
    void setContentBean(Object bean);
}
