package com.robot.mvc.core.interfaces;

/**
 * Created by laotang on 2020/1/12.
 */
public interface IProtocol {

    /**
     * 指令动作
     * @return
     */
    String getCmdKey();

    /**
     * 设备/车辆的ID
     * @return
     */
    String getDeviceId();

    /**
     * 取验证码
     * @return
     */
    String getCode();

}
