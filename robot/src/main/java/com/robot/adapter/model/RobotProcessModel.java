package com.robot.adapter.model;

import com.robot.adapter.constants.AdapterConstants;
import com.robot.adapter.enumes.Attributes;
import org.opentcs.data.TCSObjectReference;
import org.opentcs.data.model.Vehicle;
import org.opentcs.drivers.vehicle.VehicleProcessModel;
import org.opentcs.virtualvehicle.Parsers;
import org.opentcs.virtualvehicle.VelocityController;

import javax.annotation.Nonnull;

import static java.util.Objects.requireNonNull;
import static org.opentcs.util.Assertions.checkInRange;

/**
 * 车辆进程模型
 *
 * @author Laotang
 */
public class RobotProcessModel extends VehicleProcessModel {

    /**
     * 连接车辆地址
     */
    private String vehicleHost = "127.0.0.1";
    /**
     * 端口
     */
    private int vehiclePort = 60000;
    /**
     * 车辆是否空闲，默认为空闲状态
     */
    private boolean vehicleIdle = true;
    /*** 车辆空闲时，是否关闭连接，true为关闭*/
    private boolean disconnectingOnVehicleIdle = true;
    /*** 当车辆连接超时时是否自动重新连接*/
    private boolean reconnectingOnConnectionLoss = true;
    /***/
    private int reconnectDelay = 10000;
    /**
     * 车辆引用对象
     */
    private TCSObjectReference<Vehicle> vehicleReference;
    /**
     * 用于计算模拟车辆速度和当前位置的速度控制器
     */
    private final VelocityController velocityController;
    /**
     * 执行操作所需的时间
     */
    private int operatingTime;
    /**
     * 装载操作操作标识符
     */
    private final String loadOperation;
    /**
     * 卸载操作标识符
     */
    private final String unloadOperation;

    /*** 指示此通信适配器是否处于单步模式（true为单步模式，false为自动模式模式）。*/
    private boolean singleStepModeEnabled;

    /**
     * 构造函数
     *
     * @param attachedVehicle 车辆
     */
    public RobotProcessModel(@Nonnull Vehicle attachedVehicle) {
        super(attachedVehicle);
        this.vehicleReference = attachedVehicle.getReference();
        this.velocityController = new VelocityController(parseDeceleration(attachedVehicle),
                parseAcceleration(attachedVehicle),
                attachedVehicle.getMaxReverseVelocity(),
                attachedVehicle.getMaxVelocity());
        this.operatingTime = parseOperatingTime(attachedVehicle);
        this.loadOperation = extractLoadOperation(attachedVehicle);
        this.unloadOperation = extractUnloadOperation(attachedVehicle);
    }

    @Nonnull
    public TCSObjectReference<Vehicle> getVehicleReference() {
        return vehicleReference;
    }

    /**
     * 车辆是否空闲
     */
    public boolean isVehicleIdle() {
        return vehicleIdle;
    }

    /**
     * 设置车辆是否空闲，即是否下在使用，如果没有使用，则状态为空闲
     *
     * @param idle 为true时代表空闲
     */
    public void setVehicleIdle(boolean idle) {
        boolean oldValue = this.vehicleIdle;
        this.vehicleIdle = idle;

        getPropertyChangeSupport().firePropertyChange(Attributes.VEHICLE_IDLE.name(),
                oldValue,
                idle);
    }

    /**
     * 如果车辆空闲状态，是否关闭链接
     *
     * @return true为关闭链接
     */
    public boolean isDisconnectingOnVehicleIdle() {
        return disconnectingOnVehicleIdle;
    }

    /**
     * 设置车辆空闲时，是否关闭链接
     *
     * @param disconnectingOnVehicleIdle true为关闭链接
     */
    public void setDisconnectingOnVehicleIdle(boolean disconnectingOnVehicleIdle) {
        boolean oldValue = this.disconnectingOnVehicleIdle;
        this.disconnectingOnVehicleIdle = disconnectingOnVehicleIdle;

        getPropertyChangeSupport().firePropertyChange(Attributes.DISCONNECTING_ON_IDLE.name(),
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
     * 设置车辆连接超时时是否自动重新连接
     *
     * @param reconnectingOnConnectionLoss true为自动重连，默认为true
     *                                     connection times out
     */
    public void setReconnectingOnConnectionLoss(boolean reconnectingOnConnectionLoss) {
        boolean oldValue = this.reconnectingOnConnectionLoss;
        this.reconnectingOnConnectionLoss = reconnectingOnConnectionLoss;

        getPropertyChangeSupport().firePropertyChange(Attributes.RECONNECTING_ON_CONNECTION_LOSS.name(),
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

        getPropertyChangeSupport().firePropertyChange(Attributes.RECONNECT_DELAY.name(),
                oldValue,
                reconnectDelay);
    }

    /**
     * 设置通讯适配器为单步模式
     *
     * @param mode 值为true时，为单步模式
     */
    public synchronized void setSingleStepModeEnabled(final boolean mode) {
        boolean oldValue = singleStepModeEnabled;
        singleStepModeEnabled = mode;

        getPropertyChangeSupport().firePropertyChange(Attributes.SINGLE_STEP_MODE.name(),
                oldValue,
                mode);
    }

    /**
     * 当前模式是否为单步模式
     *
     * @return 返回值为true时，为单步模式
     */
    public synchronized boolean isSingleStepModeEnabled() {
        return singleStepModeEnabled;
    }


    /**
     * 返回车辆的链接地址
     *
     * @return 车辆链接地址
     */
    @Nonnull
    public synchronized String getVehicleHost() {
        return vehicleHost;
    }

    /**
     * TCP/UDP模式下，设置车辆链接地址
     *
     * @param vehicleHost TCP/UDP模式时的链接地址
     */
    public synchronized void setVehicleHost(@Nonnull String vehicleHost) {
        String oldValue = this.vehicleHost;
        this.vehicleHost = requireNonNull(vehicleHost, "vehicleHost");

        getPropertyChangeSupport().firePropertyChange(Attributes.VEHICLE_HOST.name(),
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
     *
     * @param vehiclePort TCP/UDP模式时的端口
     */
    public synchronized void setVehiclePort(int vehiclePort) {
        int oldValue = this.vehiclePort;
        this.vehiclePort = checkInRange(vehiclePort, 1024, 65535, "vehiclePort");

        getPropertyChangeSupport().firePropertyChange(Attributes.VEHICLE_PORT.name(),
                oldValue,
                vehiclePort);
    }

    /**
     * 工站最大操作时长
     *
     * @param vehicle 车辆对象
     * @return
     */
    private int parseOperatingTime(Vehicle vehicle) {
        String opTime = vehicle.getProperty(AdapterConstants.PROPKEY_OPERATING_TIME);
        // Ensure it's a positive value.
        return Math.max(Parsers.tryParseString(opTime, 5000), 1);
    }

    /**
     * 获取最大加速速度，如果用户没有指定则默认 1000(m/s²)
     *
     * @param vehicle 车辆对象
     * @return 最大加速速度
     */
    private int parseAcceleration(Vehicle vehicle) {
        String acceleration = vehicle.getProperty(AdapterConstants.PROPKEY_ACCELERATION);
        // Ensure it's a positive value.
        return Math.max(Parsers.tryParseString(acceleration, 500), 1);
    }

    /**
     * 获取最大减速速度，如果用户没有指定则默认 1000(m/s²)
     *
     * @param vehicle 车辆对象
     * @return 最大减速速度
     */
    private int parseDeceleration(Vehicle vehicle) {
        String deceleration = vehicle.getProperty(AdapterConstants.PROPKEY_DECELERATION);
        // Ensure it's a negative value.
        return Math.min(Parsers.tryParseString(deceleration, -500), -1);
    }

    private static String extractLoadOperation(Vehicle attachedVehicle) {
        String result = attachedVehicle.getProperty(AdapterConstants.PROPKEY_LOAD_OPERATION);
        if (result == null) {
            result = AdapterConstants.PROPVAL_LOAD_OPERATION_DEFAULT;
        }
        return result;
    }

    private static String extractUnloadOperation(Vehicle attachedVehicle) {
        String result = attachedVehicle.getProperty(AdapterConstants.PROPKEY_UNLOAD_OPERATION);
        if (result == null) {
            result = AdapterConstants.PROPVAL_UNLOAD_OPERATION_DEFAULT;
        }
        return result;
    }

    /**
     * 取装载标识符
     */
    public String getLoadOperation() {
        return this.loadOperation;
    }

    /**
     * 取卸载操作标识符
     */
    public String getUnloadOperation() {
        return this.unloadOperation;
    }


    /**
     * 返回最大减速速度
     *
     * @return 返回最大减速速度
     */
    public synchronized int getMaxDecceleration() {
        return velocityController.getMaxDeceleration();
    }

    /**
     * 设置最大减速速度
     *
     * @param maxDeceleration 最大减速速度
     */
    public synchronized void setMaxDeceleration(int maxDeceleration) {
        int oldValue = velocityController.getMaxDeceleration();
        velocityController.setMaxDeceleration(maxDeceleration);

        getPropertyChangeSupport().firePropertyChange(Attributes.DECELERATION.name(),
                oldValue,
                maxDeceleration);
    }

    /**
     * 返回最大加速速度
     *
     * @return 最大加速速度
     */
    public synchronized int getMaxAcceleration() {
        return velocityController.getMaxAcceleration();
    }

    /**
     * 设置最大加速速度
     *
     * @param maxAcceleration 加速速度
     */
    public synchronized void setMaxAcceleration(int maxAcceleration) {
        int oldValue = velocityController.getMaxAcceleration();
        velocityController.setMaxAcceleration(maxAcceleration);

        getPropertyChangeSupport().firePropertyChange(Attributes.ACCELERATION.name(),
                oldValue,
                maxAcceleration);
    }

    /**
     * 返回最大反向速度
     *
     * @return 返回最大反身速度
     */
    public synchronized int getMaxRevVelocity() {
        return velocityController.getMaxRevVelocity();
    }

    /**
     * 设置最大反向速度
     *
     * @param maxRevVelocity 最大反向速度
     */
    public synchronized void setMaxRevVelocity(int maxRevVelocity) {
        int oldValue = velocityController.getMaxRevVelocity();
        velocityController.setMaxRevVelocity(maxRevVelocity);

        getPropertyChangeSupport().firePropertyChange(VehicleProcessModel.Attribute.MAX_REVERSE_VELOCITY.name(),
                oldValue,
                maxRevVelocity);
    }

    /**
     * 取最大前进速度
     *
     * @return 返回最大前进速度
     */
    public synchronized int getMaxFwdVelocity() {
        return velocityController.getMaxFwdVelocity();
    }

    /**
     * 设置最大前进速度
     *
     * @param maxFwdVelocity 前进速度
     */
    public synchronized void setMaxFwdVelocity(int maxFwdVelocity) {
        int oldValue = velocityController.getMaxFwdVelocity();
        velocityController.setMaxFwdVelocity(maxFwdVelocity);

        getPropertyChangeSupport().firePropertyChange(Attributes.MAX_FORWARD_VELOCITY.name(),
                oldValue,
                maxFwdVelocity);
    }

    /**
     * 返回车辆是否暂停
     *
     * @return paused true时为暂停
     */
    public synchronized boolean isVehiclePaused() {
        return velocityController.isVehiclePaused();
    }

    /**
     * 暂停车辆(i.e. 设置速度为0)
     *
     * @param pause 值为true时，车辆暂停
     */
    public synchronized void setVehiclePaused(boolean pause) {
        boolean oldValue = velocityController.isVehiclePaused();
        velocityController.setVehiclePaused(pause);

        getPropertyChangeSupport().firePropertyChange(Attributes.VEHICLE_PAUSED.name(),
                oldValue,
                pause);
    }

    /**
     * 返回默认操作时长
     *
     * @return 默认操作时长
     */
    public synchronized int getOperatingTime() {
        return operatingTime;
    }

    /**
     * 设置默认操作时长
     *
     * @param defaultOperatingTime 新的默认操作时长
     */
    public synchronized void setOperatingTime(int defaultOperatingTime) {
        int oldValue = this.operatingTime;
        this.operatingTime = defaultOperatingTime;

        getPropertyChangeSupport().firePropertyChange(Attributes.OPERATING_TIME.name(),
                oldValue,
                defaultOperatingTime);
    }

}
