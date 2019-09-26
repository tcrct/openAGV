package com.openagv.opentcs.model;

import com.openagv.tools.Parsers;
import com.openagv.tools.ToolsKit;
import org.opentcs.data.TCSObjectReference;
import org.opentcs.data.model.Vehicle;
import org.opentcs.drivers.vehicle.VehicleProcessModel;
import org.opentcs.virtualvehicle.VelocityController;
import org.opentcs.virtualvehicle.VelocityHistory;
import org.opentcs.virtualvehicle.VelocityListener;

import javax.annotation.Nonnull;
import static com.openagv.opentcs.enums.Attribute.*;
import static java.util.Objects.requireNonNull;
import static org.opentcs.util.Assertions.checkInRange;

/**
 * 车辆进程参数模型
 * @author Laotang
 */
public class ProcessModel extends VehicleProcessModel implements VelocityListener {

    /**车辆的地址*/
    private String vehicleHost;

    /**车辆的端口*/
    private int vehiclePort;

    /**车辆连接超时是否自动进行重新连接，默认为true*/
    private boolean reconnectingOnConnectionLoss = true;

    /**车辆空闲时，是否断开连接*/
    private boolean disconnectingOnVehicleIdle = false;

    /**重新连接前，延迟多少毫秒*/
    private int reconnectDelay = 10000;

    /**车辆timeout时间，超过这默认为5000毫秒 时间，则认为连接已断开 */
    private int vehicleIdleTimeout = 5000;

    /**是否应启用日志记录,默认为false不启用*/
    private boolean loggingEnabled = false;

    /**车辆是否空闲*/
    private boolean vehicleIdle = true;

//    /**车辆最后一次发送的订单请求*/
//    private OrderRequest lastOrderSent;

    /**是否开启定时发送任务*/
    private boolean periodicStateRequestEnabled = true;

//    /**
//     * 车辆当前状态
//     */
//    private MakerwitResponse currentState;
//    /**
//     * 车辆的上一个状态
//     */
//    private MakerwitResponse previousState;

    /**
     * A reference to the vehicle.
     */
    private TCSObjectReference<Vehicle> vehicleReference;

    /**
     * Indicates whether this communication adapter is in single step mode or not (i.e. in automatic
     * mode).
     */
    private boolean singleStepModeEnabled;
    /**
     * Indicates which operation is a loading operation.
     */
    private final String loadOperation;
    /**
     * Indicates which operation is an unloading operation.
     */
    private final String unloadOperation;
    /**
     * The time needed for executing operations.
     */
    private int operatingTime;
    /**
     * The velocity controller for calculating the simulated vehicle's velocity and current position.
     */
    private final VelocityController velocityController;
    /**
     * Keeps a log of recent velocity values.
     */
    private final VelocityHistory velocityHistory = new VelocityHistory(100, 10);

    /**
     * 构造方法
     *
     * @param attachedVehicle 车辆属性
     */
    public ProcessModel(@Nonnull Vehicle attachedVehicle) {
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

    public String getVehicleHost() {
        return vehicleHost;
    }

    public void setVehicleHost(String vehicleHost) {
        String oldValue = this.vehicleHost;
        this.vehicleHost = requireNonNull(vehicleHost, "vehicleHost");
        getPropertyChangeSupport().firePropertyChange(VEHICLE_HOST.name(),
                oldValue,
                vehicleHost);
    }

    public int getVehiclePort() {
        return vehiclePort;
    }

    public void setVehiclePort(int vehiclePort) {
        int oldValue = this.vehiclePort;
        this.vehiclePort = checkInRange(vehiclePort, ToolsKit.getMinPort(), ToolsKit.getMaxPort(), "vehiclePort");
        getPropertyChangeSupport().firePropertyChange(VEHICLE_PORT.name(),
                oldValue,
                vehiclePort);
    }

    public boolean isLoggingEnabled() {
        return loggingEnabled;
    }

    public void setLoggingEnabled(boolean loggingEnabled) {
        this.loggingEnabled = loggingEnabled;
    }

    public int getVehicleIdleTimeout() {
        return vehicleIdleTimeout;
    }

    public void setVehicleIdleTimeout(int vehicleIdleTimeout) {
        this.vehicleIdleTimeout = vehicleIdleTimeout;
    }

    public boolean isReconnectingOnConnectionLoss() {
        return reconnectingOnConnectionLoss;
    }

    public void setReconnectingOnConnectionLoss(boolean reconnectingOnConnectionLoss) {
        this.reconnectingOnConnectionLoss = reconnectingOnConnectionLoss;
    }

    public int getReconnectDelay() {
        return reconnectDelay;
    }

    public void setReconnectDelay(int reconnectDelay) {
        this.reconnectDelay = reconnectDelay;
    }

    public boolean isVehicleIdle() {
        return vehicleIdle;
    }

    public void setVehicleIdle(boolean idle) {
        boolean oldValue = this.vehicleIdle;
        this.vehicleIdle = idle;

        getPropertyChangeSupport().firePropertyChange(VEHICLE_IDLE.name(),
                oldValue,
                idle);
    }

    public boolean isDisconnectingOnVehicleIdle() {
        return disconnectingOnVehicleIdle;
    }

    public void setDisconnectingOnVehicleIdle(boolean disconnectingOnVehicleIdle) {
        boolean oldValue = this.disconnectingOnVehicleIdle;
        this.disconnectingOnVehicleIdle = disconnectingOnVehicleIdle;

        getPropertyChangeSupport().firePropertyChange(DISCONNECTING_ON_IDLE.name(),
                oldValue,
                disconnectingOnVehicleIdle);
    }

    /*
    @Nonnull
    public MakerwitResponse getCurrentState() {
        return currentState;
    }

    public void setCurrentState(@Nonnull MakerwitResponse currentState) {
        MakerwitResponse oldValue = this.currentState;
        this.currentState = requireNonNull(currentState, "currentState");

        getPropertyChangeSupport().firePropertyChange(CURRENT_STATE.name(),
                oldValue,
                currentState);
    }

    @Nonnull
    public MakerwitResponse getPreviousState() {
        return previousState;
    }

    public void setPreviousState(@Nonnull MakerwitResponse previousState) {
        MakerwitResponse oldValue = this.previousState;
        this.previousState = requireNonNull(previousState, "previousState");

        getPropertyChangeSupport().firePropertyChange(com.makerwit.opentcs.enums.Attribute.PREVIOUS_STATE.name(),
                oldValue,
                previousState);
    }

    public void setLastOrderSent(@Nonnull OrderRequest telegram) {
        OrderRequest oldValue = this.lastOrderSent;
        this.lastOrderSent = telegram;

        getPropertyChangeSupport().firePropertyChange(com.makerwit.opentcs.enums.Attribute.LAST_ORDER.name(),
                oldValue,
                lastOrderSent);
    }
    */

    public boolean isPeriodicStateRequestEnabled(){
        return periodicStateRequestEnabled;
    }

    public void setPeriodicStateRequestEnabled(boolean periodicStateRequestEnabled) {
        this.periodicStateRequestEnabled = periodicStateRequestEnabled;
    }


//    @Nonnull
//    public StateResponse getCurrentState() {
//        return currentState;
//    }
//
//    public void setCurrentState(@Nonnull StateResponse currentState) {
//        StateResponse oldValue = this.currentState;
//        this.currentState = requireNonNull(currentState, "currentState");
//
//        getPropertyChangeSupport().firePropertyChange(com.makerwit.opentcs.enums.Attribute.CURRENT_STATE.name(),
//                oldValue,
//                currentState);
//    }



    public String getLoadOperation() {
        return this.loadOperation;
    }

    public String getUnloadOperation() {
        return this.unloadOperation;
    }

    /**
     * Sets this communication adapter's <em>single step mode</em> flag.
     *
     * @param mode If <code>true</code>, sets this adapter to single step mode,
     * otherwise sets this adapter to flow mode.
     */
    public synchronized void setSingleStepModeEnabled(final boolean mode) {
        boolean oldValue = singleStepModeEnabled;
        singleStepModeEnabled = mode;

        getPropertyChangeSupport().firePropertyChange(com.openagv.opentcs.enums.Attribute.SINGLE_STEP_MODE.name(),
                oldValue,
                mode);
    }

    /**
     * Returns this communication adapter's <em>single step mode</em> flag.
     *
     * @return <code>true</code> if, and only if, this adapter is currently in
     * single step mode.
     */
    public synchronized boolean isSingleStepModeEnabled() {
        return singleStepModeEnabled;
    }

    /**
     * Returns the default operating time.
     *
     * @return The default operating time
     */
    public synchronized int getOperatingTime() {
        return operatingTime;
    }

    /**
     * Sets the default operating time.
     *
     * @param defaultOperatingTime The new default operating time
     */
    public synchronized void setOperatingTime(int defaultOperatingTime) {
        int oldValue = this.operatingTime;
        this.operatingTime = defaultOperatingTime;

        getPropertyChangeSupport().firePropertyChange(com.openagv.opentcs.enums.Attribute.OPERATING_TIME.name(),
                oldValue,
                defaultOperatingTime);
    }

    /**
     * Returns the maximum deceleration.
     *
     * @return The maximum deceleration
     */
    public synchronized int getMaxDecceleration() {
        return velocityController.getMaxDeceleration();
    }

    /**
     * Sets the maximum deceleration.
     *
     * @param maxDeceleration The new maximum deceleration
     */
    public synchronized void setMaxDeceleration(int maxDeceleration) {
        int oldValue = velocityController.getMaxDeceleration();
        velocityController.setMaxDeceleration(maxDeceleration);

        getPropertyChangeSupport().firePropertyChange(com.openagv.opentcs.enums.Attribute.DECELERATION.name(),
                oldValue,
                maxDeceleration);
    }

    /**
     * Returns the maximum acceleration.
     *
     * @return The maximum acceleration
     */
    public synchronized int getMaxAcceleration() {
        return velocityController.getMaxAcceleration();
    }

    /**
     * Sets the maximum acceleration.
     *
     * @param maxAcceleration The new maximum acceleration
     */
    public synchronized void setMaxAcceleration(int maxAcceleration) {
        int oldValue = velocityController.getMaxAcceleration();
        velocityController.setMaxAcceleration(maxAcceleration);

        getPropertyChangeSupport().firePropertyChange(com.openagv.opentcs.enums.Attribute.ACCELERATION.name(),
                oldValue,
                maxAcceleration);
    }

    /**
     * Returns the maximum reverse velocity.
     *
     * @return The maximum reverse velocity.
     */
    public synchronized int getMaxRevVelocity() {
        return velocityController.getMaxRevVelocity();
    }

    /**
     * Sets the maximum reverse velocity.
     *
     * @param maxRevVelocity The new maximum reverse velocity
     */
    public synchronized void setMaxRevVelocity(int maxRevVelocity) {
        int oldValue = velocityController.getMaxRevVelocity();
        velocityController.setMaxRevVelocity(maxRevVelocity);

        getPropertyChangeSupport().firePropertyChange(com.openagv.opentcs.enums.Attribute.MAX_REVERSE_VELOCITY.name(),
                oldValue,
                maxRevVelocity);
    }

    /**
     * Returns the maximum forward velocity.
     *
     * @return The maximum forward velocity.
     */
    public synchronized int getMaxFwdVelocity() {
        return velocityController.getMaxFwdVelocity();
    }

    /**
     * Sets the maximum forward velocity.
     *
     * @param maxFwdVelocity The new maximum forward velocity.
     */
    public synchronized void setMaxFwdVelocity(int maxFwdVelocity) {
        int oldValue = velocityController.getMaxFwdVelocity();
        velocityController.setMaxFwdVelocity(maxFwdVelocity);

        getPropertyChangeSupport().firePropertyChange(com.openagv.opentcs.enums.Attribute.MAX_FORWARD_VELOCITY.name(),
                oldValue,
                maxFwdVelocity);
    }

    /**
     * Returns whether the vehicle is paused.
     *
     * @return paused
     */
    public synchronized boolean isVehiclePaused() {
        return velocityController.isVehiclePaused();
    }

    /**
     * Pause the vehicle (i.e. set it's velocity to zero).
     *
     * @param pause True, if vehicle shall be paused. False, otherwise.
     */
    public synchronized void setVehiclePaused(boolean pause) {
        boolean oldValue = velocityController.isVehiclePaused();
        velocityController.setVehiclePaused(pause);

        getPropertyChangeSupport().firePropertyChange(com.openagv.opentcs.enums.Attribute.VEHICLE_PAUSED.name(),
                oldValue,
                pause);

        setSingleStepModeEnabled(pause);
    }

    /**
     * Returns the virtual vehicle's velocity controller.
     *
     * @return The virtual vehicle's velocity controller.
     */
    @Nonnull
    public VelocityController getVelocityController() {
        return velocityController;
    }

    /**
     * Returns a log of recent velocity values of the vehicle.
     *
     * @return A log of recent velocity values.
     */
    @Nonnull
    public VelocityHistory getVelocityHistory() {
        return velocityHistory;
    }

    @Override
    public void addVelocityValue(int velocityValue) {
        // Store the new value in the history...
        velocityHistory.addVelocityValue(velocityValue);
        // ...and let all observers know about it.
        getPropertyChangeSupport().firePropertyChange(com.openagv.opentcs.enums.Attribute.VELOCITY_HISTORY.name(),
                null,
                velocityHistory);
    }

    private int parseOperatingTime(Vehicle vehicle) {
        String opTime = vehicle.getProperty(AdapterConstants.PROPKEY_OPERATING_TIME);
        // Ensure it's a positive value.
        return Math.max(Parsers.tryParseString(opTime, 5000), 1);
    }

    /**
     * Gets the maximum acceleration. If the user did not specify any, 1000(m/s²) is returned.
     *
     * @param vehicle the vehicle
     * @return the maximum acceleration.
     */
    private int parseAcceleration(Vehicle vehicle) {
        String acceleration = vehicle.getProperty(AdapterConstants.PROPKEY_ACCELERATION);
        // Ensure it's a positive value.
        return Math.max(Parsers.tryParseString(acceleration, 500), 1);
    }

    /**
     * Gets the maximum decceleration. If the user did not specify any, 1000(m/s²) is returned.
     *
     * @param vehicle the vehicle
     * @return the maximum decceleration.
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
}

