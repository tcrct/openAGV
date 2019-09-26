package com.openagv.opentcs.commands;

import com.openagv.opentcs.adapter.CommAdapter;
import org.opentcs.drivers.vehicle.AdapterCommand;
import org.opentcs.drivers.vehicle.VehicleCommAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SetVehiclePausedCommand implements AdapterCommand {

    private static final Logger logger = LoggerFactory.getLogger(SetVehiclePausedCommand.class);

    private final boolean paused;

    public SetVehiclePausedCommand(boolean paused) {
        this.paused = paused;
    }

    @Override
    public void execute(VehicleCommAdapter adapter) {
        if (!(adapter instanceof CommAdapter)) {
            return;
        }

        CommAdapter commAdapter = (CommAdapter) adapter;
        commAdapter.getProcessModel().setVehiclePaused(paused);
        logger.warn("暂停车辆: {}", paused);
    }
}
