package com.openagv.opentcs.enums;

/**
 * 模型属性枚举
 *
 * @author Laotang
 */
public enum Attribute {
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


    /**
     * Indicates a change of the virtual vehicle's single step mode setting.
     */
    SINGLE_STEP_MODE,
    /**
     * Indicates a change of the virtual vehicle's default operating time.
     */
    OPERATING_TIME,
    /**
     * Indicates a change of the virtual vehicle's maximum acceleration.
     */
    ACCELERATION,
    /**
     * Indicates a change of the virtual vehicle's maximum deceleration.
     */
    DECELERATION,
    /**
     * Indicates a change of the virtual vehicle's maximum forward velocity.
     */
    MAX_FORWARD_VELOCITY,
    /**
     * Indicates a change of the virtual vehicle's maximum reverse velocity.
     */
    MAX_REVERSE_VELOCITY,
    /**
     * Indicates a change of the virtual vehicle's paused setting.
     */
    VEHICLE_PAUSED,
    /**
     * Indicates a change of the virtual vehicle's velocity history.
     */
    VELOCITY_HISTORY,

    ;
}
