/**
 * Copyright (c) The openTCS Authors.
 * <p>
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package com.robot.commands;

import com.robot.adapter.RobotCommAdapter;
import org.opentcs.drivers.vehicle.AdapterCommand;
import org.opentcs.drivers.vehicle.VehicleCommAdapter;

/**
 * A command to trigger the comm adapter in single step mode.
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
public class TriggerCommand
        implements AdapterCommand {

    @Override
    public void execute(VehicleCommAdapter adapter) {
        if (!(adapter instanceof RobotCommAdapter)) {
            return;
        }

        RobotCommAdapter commAdapter = (RobotCommAdapter) adapter;
        commAdapter.trigger();
    }
}
