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
    /**车辆是否空闲，默认为空闲状态*/
    private boolean vehicleIdle = true;
    /*** 车辆空闲时，是否关闭连接，true为关闭*/
    private boolean disconnectingOnVehicleIdle = true;
    /*** 当车辆连接超时时是否自动重新连接*/
    private boolean reconnectingOnConnectionLoss = true;
    /**
     *
     */
    private int reconnectDelay = 10000;

    /**
     * 构造函数
     *
     * @param attachedVehicle 车辆
     */
    public AgvProcessModel(@Nonnull Vehicle attachedVehicle) {
        super(attachedVehicle);
    }

    /**车辆是否空闲*/
    public boolean isVehicleIdle() {
        return vehicleIdle;
    }

    /**
     * 设置车辆是否空闲，即是否下在使用，如果没有使用，则状态为空闲
     *
     * @param idle  为true时代表空闲
     */
    public void setVehicleIdle(boolean idle) {
        boolean oldValue = this.vehicleIdle;
        this.vehicleIdle = idle;

        getPropertyChangeSupport().firePropertyChange(Attribute.VEHICLE_IDLE.name(),
                oldValue,
                idle);
    }

    /**
     *  如果车辆空闲状态，是否关闭链接
     *
     * @return true为关闭链接
     */
    public boolean isDisconnectingOnVehicleIdle() {
        return disconnectingOnVehicleIdle;
    }

    /**
     * 设置车辆空闲时，是否关闭链接
     *
     * @param disconnectingOnVehicleIdle  true为关闭链接
     */
    public void setDisconnectingOnVehicleIdle(boolean disconnectingOnVehicleIdle) {
        boolean oldValue = this.disconnectingOnVehicleIdle;
        this.disconnectingOnVehicleIdle = disconnectingOnVehicleIdle;

        getPropertyChangeSupport().firePropertyChange(Attribute.DISCONNECTING_ON_IDLE.name(),
                oldValue,
                disconnectingOnVehicleIdle);
    }

    /**
     * 当车辆连接超时时是否自动重新连接
     *
     * @return true为自动重新连接
     */
    public boolean isReconnectingOnConnectionLoss() {
        return reconnectingOnConnectionLoss;
    }

    /**
     *  设置车辆连接超时时是否自动重新连接
     *
     * @param reconnectingOnConnectionLoss true为自动重连，默认为true
     * connection times out
     */
    public void setReconnectingOnConnectionLoss(boolean reconnectingOnConnectionLoss) {
        boolean oldValue = this.reconnectingOnConnectionLoss;
        this.reconnectingOnConnectionLoss = reconnectingOnConnectionLoss;

        getPropertyChangeSupport().firePropertyChange(Attribute.RECONNECTING_ON_CONNECTION_LOSS.name(),
                oldValue,
                reconnectingOnConnectionLoss);
    }

    /**
     * 重新链接的时间间隔，毫秒为单位
     *
     * @return 毫秒数
     */
    public int getReconnectDelay() {
        return reconnectDelay;
    }

    /**
     * 设置重新连接的时间间隔，毫秒为单位
     *
     * @param reconnectDelay 毫秒时间数
     */
    public void setReconnectDelay(int reconnectDelay) {
        int oldValue = this.reconnectDelay;
        this.reconnectDelay = checkInRange(reconnectDelay, 1, Integer.MAX_VALUE, "reconnectDelay");

        getPropertyChangeSupport().firePropertyChange(Attribute.RECONNECT_DELAY.name(),
                oldValue,
                reconnectDelay);
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
     * @param vehicleHost TCP/UDP模式时的链接地址
     */
    public synchronized void setVehicleHost(@Nonnull String vehicleHost) {
        String oldValue = this.vehicleHost;
        this.vehicleHost = requireNonNull(vehicleHost, "vehicleHost");

        getPropertyChangeSupport().firePropertyChange(Attribute.VEHICLE_HOST.name(),
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

        getPropertyChangeSupport().firePropertyChange(Attribute.VEHICLE_PORT.name(),
                oldValue,
                vehiclePort);
    }

    public static enum Attribute {
        CURRENT_STATE,
        PREVIOUS_STATE,
        LAST_ORDER,
        VEHICLE_HOST,
        VEHICLE_PORT,
        PERIODIC_STATE_REQUESTS_ENABLED,
        PERIOD_STATE_REQUESTS_INTERVAL,
        VEHICLE_IDLE_TIMEOUT,
        VEHICLE_IDLE,
        DISCONNECTING_ON_IDLE,
        RECONNECTING_ON_CONNECTION_LOSS,
        LOGGING_ENABLED,
        RECONNECT_DELAY,

        SINGLE_STEP_MODE,

        ;
    }

}
