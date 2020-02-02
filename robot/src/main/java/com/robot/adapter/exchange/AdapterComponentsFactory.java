package com.robot.adapter.exchange;

import com.robot.adapter.RobotCommAdapter;
import org.opentcs.data.model.Vehicle;

/**
 * 适配器组件工厂
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
    RobotCommAdapter createCommAdapter(Vehicle vehicle);

}
