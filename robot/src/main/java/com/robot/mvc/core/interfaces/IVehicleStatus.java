package com.robot.mvc.core.interfaces;

/**
 * Created by laotang on 2020/2/19.
 */
public interface IVehicleStatus {

    /**
     * 立即停车，当上报的点与移动队列里第1位元素不一致时，可能需要作停车处理
     *
     * @param protocol 协议对象
     */
    void stopVehicle(IProtocol protocol);

    /**
     * 初始车辆位置
     *
     * @param name 车辆名称
     */
    void initVehiclePosition(String name);

}
