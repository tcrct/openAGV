package com.robot.adapter.exchange;

import com.robot.utils.SettingUtil;
import org.opentcs.drivers.vehicle.VehicleCommAdapterDescription;

/**
 * 通讯适配器名称
 *
 * @author Laotang
 */
public class RobotAdapterDescription extends VehicleCommAdapterDescription {

    @Override
    public String getDescription() {
        return SettingUtil.getString("name", "adapter", "robot");
    }

}
