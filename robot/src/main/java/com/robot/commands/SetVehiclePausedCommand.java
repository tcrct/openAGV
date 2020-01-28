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
 * A command to pause/unpause the vehicle.
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
public class SetVehiclePausedCommand
        implements AdapterCommand {

    /**
     * Whether to pause/unpause the vehicle.
     */
    private final boolean paused;

    /**
     * Creates a new instance.
     *
     * @param paused Whether to pause/unpause the vehicle.
     */
    public SetVehiclePausedCommand(boolean paused) {
        this.paused = paused;
    }

    @Override
    public void execute(VehicleCommAdapter adapter) {
        if (!(adapter instanceof RobotCommAdapter)) {
            return;
        }

        RobotCommAdapter commAdapter = (RobotCommAdapter) adapter;
        // 待实现
//    commAdapter.getProcessModel().setVehiclePaused(paused);
    }

}
