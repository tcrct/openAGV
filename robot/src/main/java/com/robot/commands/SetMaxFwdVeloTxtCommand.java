package com.robot.commands;

import com.robot.adapter.RobotCommAdapter;
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
     * @param value 前进速度
     */
    public SetMaxFwdVeloTxtCommand(int value) {
        this.value = value;
    }

    @Override
    public void execute(VehicleCommAdapter adapter) {
        if (!(adapter instanceof RobotCommAdapter)) {
            return;
        }
        RobotCommAdapter commAdapter = (RobotCommAdapter) adapter;
        commAdapter.getProcessModel().setVehicleMaxVelocity(value);
    }
}
