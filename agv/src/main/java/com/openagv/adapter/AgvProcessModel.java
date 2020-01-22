package com.openagv.adapter;

import org.opentcs.data.model.Vehicle;
import org.opentcs.drivers.vehicle.VehicleProcessModel;

import javax.annotation.Nonnull;

import static java.util.Objects.requireNonNull;
import static org.opentcs.util.Assertions.checkInRange;

/**
 * 车辆进程模型
 *
 * @author Laotang
 */
public class AgvProcessModel extends VehicleProcessModel {

    /** 连接车辆地址*/
    private String vehicleHost = "127.0.0.1";
    /** 端口*/
    private int vehiclePort = 60000;

    /**
     * Creates a new instance.
     *
     * @param attachedVehicle The vehicle attached to the new instance.
     */
    public AgvProcessModel(@Nonnull Vehicle attachedVehicle) {
        super(attachedVehicle);
    }

    /**
     * 返回车辆的链接地址
     * @return 车辆链接地址
     */
    @Nonnull
    public synchronized String getVehicleHost() {
        return vehicleHost;
    }

    /**
     * TCP/UDP模式下，设置车辆链接地址
     * @param vehiclePort TCP/UDP模式时的链接地址
     */
    public synchronized void setVehicleHost(@Nonnull String vehicleHost) {
        String oldValue = this.vehicleHost;
        this.vehicleHost = requireNonNull(vehicleHost, "vehicleHost");

        getPropertyChangeSupport().firePropertyChange("VEHICLE_HOST",
                oldValue,
                vehicleHost);
    }

    /**
     * 返回需要链接的端口号
     *
     * @return 端口
     */
    public synchronized int getVehiclePort() {
        return vehiclePort;
    }

    /**
     * TCP/UDP模式下，设置车辆端口
     * @param vehiclePort TCP/UDP模式时的端口
     */
    public synchronized void setVehiclePort(int vehiclePort) {
        int oldValue = this.vehiclePort;
        this.vehiclePort = checkInRange(vehiclePort, 1024, 65535, "vehiclePort");

        getPropertyChangeSupport().firePropertyChange("VEHICLE_PORT",
                oldValue,
                vehiclePort);
    }
}
