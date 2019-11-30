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

/**
 * 设置车辆方向
 *
 * @author Laotang
 */
public class SetOrientationAngleCommand
    implements AdapterCommand {

  /**
   * The orientation angle to set.
   */
  private final double angle;

  /**
   * Creates a new instance.
   *
   * @param angle The orientation angle to set.
   */
  public SetOrientationAngleCommand(double angle) {
    this.angle = angle;
  }

  @Override
  public void execute(VehicleCommAdapter adapter) {
    if (!(adapter instanceof CommAdapter)) {
      return;
    }
    adapter.getProcessModel().setVehicleOrientationAngle(angle);
  }
}
