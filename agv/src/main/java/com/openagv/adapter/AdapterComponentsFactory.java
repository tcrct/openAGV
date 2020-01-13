package com.openagv.adapter;

import org.opentcs.data.model.Vehicle;

/**
 * AdapterComponentsFactory
 *
 * @blame OpenAgv
 */
public interface AdapterComponentsFactory {

    /**
     * 为车辆返回一个指定的适配器
     *
     * @param 车辆
     * @return 为车辆返回一个指定的RobotCommAdapter适配器
     */
    AgvCommAdapter createRobotCommAdapter(Vehicle vehicle);

}
