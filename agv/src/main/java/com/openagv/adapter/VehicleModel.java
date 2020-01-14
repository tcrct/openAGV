package com.openagv.adapter;

import org.opentcs.data.model.Vehicle;
import org.opentcs.drivers.vehicle.VehicleProcessModel;

import javax.annotation.Nonnull;

/**
 * 车辆模型
 */
public class VehicleModel extends VehicleProcessModel {
    /**
     * Creates a new instance.
     *
     * @param attachedVehicle The vehicle attached to the new instance.
     */
    public VehicleModel(@Nonnull Vehicle attachedVehicle) {
        super(attachedVehicle);
    }
}
