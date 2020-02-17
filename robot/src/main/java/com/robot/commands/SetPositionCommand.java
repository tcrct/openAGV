package com.robot.commands;

import org.opentcs.drivers.vehicle.AdapterCommand;
import org.opentcs.drivers.vehicle.VehicleCommAdapter;

import javax.annotation.Nullable;

/**
 * 设置车辆位置命令
 *
 * @author Laotang
 */
public class SetPositionCommand implements AdapterCommand {

    /**
     * 需要设置位置的点
     */
    private final String position;

    /**
     * 构造方法
     *
     * @param position 位置点
     */
    public SetPositionCommand(@Nullable String position) {
        this.position = position;
    }

    @Override
    public void execute(VehicleCommAdapter adapter) {
        adapter.getProcessModel().setVehiclePosition(position);
    }
}
