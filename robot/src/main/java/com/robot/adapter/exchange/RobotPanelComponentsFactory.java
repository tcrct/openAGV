package com.robot.adapter.exchange;

import com.robot.adapter.model.RobotVehicleModelTO;
import org.opentcs.components.kernel.services.VehicleService;

/**
 * 面板组件工厂
 *
 * @author Laotang
 */
public interface RobotPanelComponentsFactory {

    /**
     * 创建流程模型
     *
     * @param vehicleModelTO model要表示的流程模型
     * @param vehicleService 用于与通信适配器交互的车辆服务
     * @return
     */
    RobotCommAdapterPanel createControlPanel(RobotVehicleModelTO vehicleModelTO, VehicleService vehicleService);
}
