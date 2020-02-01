/**
 * Copyright (c) Fraunhofer IML
 */
package com.robot.adapter;

import com.robot.mvc.utils.SettingUtil;
import org.opentcs.drivers.vehicle.VehicleCommAdapterDescription;

/**
 * 通讯适配器名称
 *
 * @author Laotang
 */
public class AdapterDescription extends VehicleCommAdapterDescription {

  @Override
  public String getDescription() {
      return SettingUtil.getString("name", "adapter", "OpenAgv");
  }
}
