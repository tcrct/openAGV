/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package com.openagv.opentcs.commands;

import com.openagv.opentcs.adapter.CommAdapter;
import org.opentcs.drivers.vehicle.AdapterCommand;
import org.opentcs.drivers.vehicle.VehicleCommAdapter;

import javax.annotation.Nullable;

/**
 * 设置车辆位置
 *
 * @author Laotang
 */
public class SetPositionCommand
    implements AdapterCommand {

  /**
   * The position to set.
   */
  private final String position;

  /**
   * Creates a new instance.
   *
   * @param position The position to set.
   */
  public SetPositionCommand(@Nullable String position) {
    this.position = position;
  }

  @Override
  public void execute(VehicleCommAdapter adapter) {
    if (!(adapter instanceof CommAdapter)) {
      return;
    }
//    System.out.println("adapter.getProcessModel().getName():" + adapter.getProcessModel().getName());
    adapter.getProcessModel().setVehiclePosition(position);
  }
}
