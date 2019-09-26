package com.openagv.opentcs.commands;

import com.openagv.opentcs.adapter.CommAdapter;
import org.opentcs.drivers.vehicle.AdapterCommand;
import org.opentcs.drivers.vehicle.VehicleCommAdapter;

public class TriggerCommand
        implements AdapterCommand {

    @Override
    public void execute(VehicleCommAdapter adapter) {
        if (!(adapter instanceof CommAdapter)) {
            return;
        }

        CommAdapter loopbackAdapter = (CommAdapter) adapter;
        loopbackAdapter.trigger();
    }
}
