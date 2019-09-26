package com.openagv.opentcs.adapter;

import com.openagv.opentcs.model.VehicleModelTO;
import org.opentcs.components.kernel.services.VehicleService;

/**
 * 面板组件工厂
 */
public interface PanelComponentsFactory {

    /**
     *创建流程模型
     * @param processModel model要表示的流程模型
     * @param vehicleService 用于与通信适配器交互的车辆服务
     * @return
     */
    CommAdapterPanel createControlPanel(VehicleModelTO processModel, VehicleService vehicleService);
}
