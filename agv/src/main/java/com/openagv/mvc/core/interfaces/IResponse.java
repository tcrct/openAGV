package com.openagv.mvc.core.interfaces;

/**
 * Created by laotang on 2020/1/12.
 */
public interface IResponse extends ITelegram {

    /**响应对应的ID，Mongodb ObjectId格式*/
    String getId();
    /**车辆或设备的唯一标识*/
    String getDeviceId();
    /**操作命令*/
    String getCmdKey();
    /**设置响应状态值*/
    void setStatus(int status);
    /**取响应状态值*/
    int getStatus();
    /**写入响应对象*/
    void write(Object message);
    /**取响应异常*/
    Exception getException();

    /**
     * 取握手报文的CODE
     * 即生成请求下发后，握手应答报文的code
     * 用于重发机制时，当车辆或设备回复后，需要根据该值来判断请求与响应是否一致
     *
     * @return
     */
    String getHandshakeCode();

    /**比较请求与响应是否对应*/
    default boolean isResponseTo(IRequest request) {
        return getId().equals(request.getId());
    }
}
