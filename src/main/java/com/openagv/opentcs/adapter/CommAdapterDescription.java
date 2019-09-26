package com.openagv.opentcs.adapter;

import com.openagv.tools.SettingUtils;
import org.opentcs.drivers.vehicle.VehicleCommAdapterDescription;

/**
 * 通讯适配器名称
 */
public class CommAdapterDescription extends VehicleCommAdapterDescription {
    @Override
    public String getDescription() {
        return SettingUtils.getString("adapter.name","MyAdapter");
    }
}
