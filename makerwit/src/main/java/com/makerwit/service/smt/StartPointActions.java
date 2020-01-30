package com.makerwit.service.smt;


import com.makerwit.core.requests.get.GetMtRequest;
import com.robot.mvc.core.annnotations.Action;
import com.robot.mvc.core.interfaces.IActionCommand;
import com.makerwit.core.component.BaseActions;

import java.util.Arrays;
import java.util.List;

@Action
public class StartPointActions extends BaseActions {
    /**
     * 动作组合名称，要与工厂概述中的动作名称一样
     */
    private static final String ACTION_KEY = "StartPoint";
    /**
     * 车辆的串口模块名称
     */
    private static final String VEHICLE_ID = "A009";
    /**
     * 设备的串口模块名称
     */
    private static final String DEVICE_ID = "B002";

    @Override
    public String actionKey() {
        return ACTION_KEY;
    }

    @Override
    public String vehicleId() {
        return VEHICLE_ID;
    }

    @Override
    public String deviceId() {
        return DEVICE_ID;
    }

    /**
     * 车辆起点执行的动作：
     * 1，发送查询物料状态请求到车辆
     * 2，判断车辆上是否有货物，若没有则一直在等待
     *
     * @param requestList 要执行的请求指令的有序数组
     */
    @Override
    public void add(List<IActionCommand> requestList) {
        requestList.addAll(Arrays.asList(
                new GetMtRequest(VEHICLE_ID, "0")
//           new RptMtResponse(VEHICLE_ID, "1")
        ));
    }
}
