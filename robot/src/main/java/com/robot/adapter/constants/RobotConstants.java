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

    /**
     * 协议服务类的父类名称
     */
    public static final String PROTOCOL_SERVICE_NAME_FIELD = "ProtocolService";

    // 订单ID前缀标符串
    public static final String ORDER_ID_PREFIX = "Robot_";
    // 车辆或设备在控制台设置名称参数所使用的标识符
    public static final String NAME_FIELD = "name";
    // 车辆或设备在控制台设置地址参数所使用的标识符
    public static final String HOST_FIELD = "host";
    // 车辆或设备在控制台设置端口参数所使用的标识符
    public static final String PORT_FIELD = "port";
    // 车辆ID
    public static final String VEHICLE_ID_FIELD = "vehicleId";
    // 设备终端ID
    public static final String DEVICE_ID_FIELD = "deviceId";

    // IdEntity里的source字段里的其中一个标识符
    public static final String TERMINAL_FIELD = "terminal";
    // 标识符，是否是业务主动提交的传感器请求
    public static final String TERMINAL_SUBMIT_RPTMT_FIELD = "terminal_submit_rptmt";
    // BaseActions里用于标识actionKey的关键字
    public static final String ACTION_KEY_FIELD = "actionKey";
    // 动作指令集如需要在运行时实时取得参数，则需要在ActionRequest里设置该值作为标识符
    public static final String DYNAMIC_PARAM_FIELD = "dynamic_param";

    // 根据用户名称
    public static String ROOT_FIELD = "root";
    // 管理员用户名称
    public static final String ADMIN_FIELD = "admin";
    // 系统默认的用户密码
    public static final String DEFAULT_PASSWORD = "robot";
    // 根据用户访问密钥，非常重要
    public static final String ROOT_SECRET_KEY = "x-api-secret-key";
}
