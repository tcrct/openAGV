/**
 * Copyright (c) The openTCS Authors.
 * <p>
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package com.robot.commands;

import org.opentcs.data.model.Vehicle;
import org.opentcs.drivers.vehicle.AdapterCommand;
import org.opentcs.drivers.vehicle.VehicleCommAdapter;

import javax.annotation.Nonnull;

import static java.util.Objects.requireNonNull;

/**
 * A command to set a vehicle's state.
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
public class SetStateCommand
        implements AdapterCommand {

    /**
     * The vehicle state to set.
     */
    private final Vehicle.State state;

    /**
     * Creates a new instance.
     *
     * @param state The vehicle state to set.
     */
    public SetStateCommand(@Nonnull Vehicle.State state) {
        this.state = requireNonNull(state, "state");
    }

    @Override
    public void execute(VehicleCommAdapter adapter) {
        adapter.getProcessModel().setVehicleState(state);
    }
}
