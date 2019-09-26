package com.openagv.opentcs.commands;

import com.openagv.opentcs.adapter.CommAdapter;
import org.opentcs.drivers.vehicle.AdapterCommand;
import org.opentcs.drivers.vehicle.VehicleCommAdapter;

public class SetMaxFwdVeloTxtCommand implements AdapterCommand {

    /**
     *
     */
    private final int value;

    /**
     * Creates a new instance.
     *
     * @param level The energy level to set.
     */
    public SetMaxFwdVeloTxtCommand(int value) {
        this.value = value;
    }

    @Override
    public void execute(VehicleCommAdapter adapter) {
        if (!(adapter instanceof CommAdapter)) {
            return;
        }
        CommAdapter commAdapter = (CommAdapter) adapter;
        commAdapter.getProcessModel().setVehicleMaxVelocity(value);
    }
}
