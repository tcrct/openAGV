package com.robot.adapter.constants;

public class RobotConstants {

    /**
     * The key of the vehicle property that specifies the vehicle's initial position.
     */
    public static final String PROPKEY_INITIAL_POSITION = "loopback:initialPosition";
    /**
     * The key of the vehicle property that specifies the default operating time.
     */
    public static final String PROPKEY_OPERATING_TIME = "loopback:operatingTime";
    /**
     * The key of the vehicle property that specifies which operation loads the load handling device.
     */
    public static final String PROPKEY_LOAD_OPERATION = "loopback:loadOperation";
    /**
     * The default value of the load operation property.
     */
    public static final String PROPVAL_LOAD_OPERATION_DEFAULT = "Load cargo";
    /**
     * The key of the vehicle property that specifies which operation unloads the load handling device.
     */
    public static final String PROPKEY_UNLOAD_OPERATION = "loopback:unloadOperation";
    /**
     * The default value of the unload operation property.
     */
    public static final String PROPVAL_UNLOAD_OPERATION_DEFAULT = "Unload cargo";
    /**
     * The key of the vehicle property that specifies the maximum acceleration of a vehicle.
     */
    public static final String PROPKEY_ACCELERATION = "loopback:acceleration";
    /**
     * The key of the vehicle property that specifies the maximum decceleration of a vehicle.
     */
    public static final String PROPKEY_DECELERATION = "loopback:deceleration";

    /**
     * An operation constant for doing nothing.
     */
    public static final String OP_NOP = "NOP";
    /**
     * An operation constant for parking the vehicle.
     */
    public static final String OP_PARK = "PARK";
    /**
     * An operation constant for sending the vehicle to a point without a location associated to it.
     */
    public static final String OP_MOVE = "MOVE";

    public static final String ORDER_ID_PREFIX = "Robot_";

    public static final String TERMINAL_FIELD = "terminal";

    public static String ROOT_FIELD = "root";
    public static final String ADMIN_FIELD = "admin";
}
