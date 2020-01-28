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
 *开启或关闭通讯适配器为单步模式
 * @author Martin Grzenia (Fraunhofer IML)
 */
public class SetSingleStepModeEnabledCommand
        implements AdapterCommand {

    /**
     * Whether to enable/disable single step mode.
     */
    private final boolean enabled;

    /**
     * Creates a new instance.
     *
     * @param enabled Whether to enable/disable single step mode.
     */
    public SetSingleStepModeEnabledCommand(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public void execute(VehicleCommAdapter adapter) {
        if (!(adapter instanceof RobotCommAdapter)) {
            return;
        }

        RobotCommAdapter commAdapter = (RobotCommAdapter) adapter;
        commAdapter.getProcessModel().setSingleStepModeEnabled(enabled);
    }

}
