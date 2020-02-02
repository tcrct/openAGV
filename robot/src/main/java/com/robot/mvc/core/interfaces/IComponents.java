package com.robot.mvc.core.interfaces;

/**
 * OpenAGV需要用到第三方实现的组件接口
 * 每一个方法都需要实现
 *
 * @author Laotang
 */
public interface IComponents {

    /***
     * 取协议解析器对象
     * @return
     */
    IProtocolMatcher getProtocolMatcher();

    /***
     * 取重复发送处理对象
     * @return
     */
    IRepeatSend getRepeatSend();

    /**
     * 立即停车
     *
     * @param protocol 协议对象
     */
    void stopVehicle(IProtocol protocol);

}
