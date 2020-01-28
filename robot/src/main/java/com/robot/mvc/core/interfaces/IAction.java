package com.robot.mvc.core.interfaces;


/**
 * 工站动作接口
 *
 * @author Laotang
 * @blame Android Team
 */
public interface IAction {

    /**
     * 动作名称
     *
     * @return
     */
    String actionKey();

    /**
     * 车辆ID
     */
    String vehicleId();

    /***
     * 工站设备ID
     * @return
     */
    String deviceId();

    /**
     *执行操作
     */
//    void execute() throws Exception;

}
