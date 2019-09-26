package com.openagv.opentcs.commands;

import com.openagv.opentcs.adapter.CommAdapter;
import org.opentcs.drivers.vehicle.AdapterCommand;
import org.opentcs.drivers.vehicle.VehicleCommAdapter;

public class SetEnergyLevelCommand implements AdapterCommand {

    /**
     * The energy level to set.
     */
    private final int level;

    /**
     * Creates a new instance.
     *
     * @param level The energy level to set.
     */
    public SetEnergyLevelCommand(int level) {
        this.level = level;
    }

    @Override
    public void execute(VehicleCommAdapter adapter) {
        if (!(adapter instanceof CommAdapter)) {
            return;
        }
        CommAdapter commAdapter = (CommAdapter) adapter;
        commAdapter.getProcessModel().setVehicleEnergyLevel(level);
    }
}
