package com.robot.commands;

import com.robot.adapter.RobotCommAdapter;
import org.opentcs.drivers.vehicle.AdapterCommand;
import org.opentcs.drivers.vehicle.VehicleCommAdapter;

/***
 * 下一步
 */
public class NextStepButtonCommand
        implements AdapterCommand {

    @Override
    public void execute(VehicleCommAdapter adapter) {
        if (!(adapter instanceof RobotCommAdapter)) {
            return;
        }

        RobotCommAdapter commAdapter = (RobotCommAdapter) adapter;
//        commAdapter.nextStepButton();
    }
}
