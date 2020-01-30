package com.robot.mvc.core.interfaces;

import com.robot.mvc.core.exceptions.RobotException;

/**
 * Created by laotang on 2020/1/30.
 */
public interface IActionCallback {

    /**
     * 回调执行工站指令动作
     *
     * @param actionKey 工站命名，须保证唯一性
     * @param requestId 工站指令请求ID
     * @param code      握手验证码，用于验证是否需要进行回调操作
     * @param vehicleId 车辆ID,当指令执行完毕 , 根据该车辆ID查找对应的通讯适配器进行车辆移动操作
     * @throws RobotException
     */
    void call(String actionKey, String requestId, String code, String vehicleId) throws RobotException;

}
