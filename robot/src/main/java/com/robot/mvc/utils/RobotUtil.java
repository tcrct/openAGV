package com.robot.mvc.utils;

import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import com.robot.RobotContext;
import com.robot.adapter.RobotCommAdapter;
import com.robot.adapter.enumes.OperatingState;
import com.robot.contrib.netty.comm.NetChannelType;
import com.robot.contrib.netty.comm.RunType;
import com.robot.mvc.core.enums.ReqType;
import com.robot.mvc.core.exceptions.RobotException;
import com.robot.mvc.core.interfaces.IAction;
import com.robot.mvc.core.interfaces.IRequest;
import com.robot.mvc.helpers.RouteHelper;
import com.robot.mvc.model.Route;
import org.opentcs.components.kernel.services.TCSObjectService;
import org.opentcs.data.model.Location;
import org.opentcs.data.model.Path;
import org.opentcs.data.model.Point;
import org.opentcs.data.model.Vehicle;

import java.util.*;

/**
 * Created by laotang on 2020/1/22.
 */
public class RobotUtil {

    private static final Log LOG = LogFactory.get();

    /**
     * 是否开发模式
     *
     * @return
     */
    public static boolean isDevMode() {
        return SettingUtil.getBoolean("dev.mode", false);
    }

    /**
     * 下发路径指令关键字
     * 取移动协议指令关键字，用于生成移动请求时，设置cmdKey值
     *
     * @return 关键字
     */
    public static String getMoveProtocolKey() {
        String moveCmdKey = SettingUtil.getString("move.request.cmd");
        if (ToolsKit.isEmpty(moveCmdKey)) {
            throw new RobotException("下发路径指令关键字不能为空，请先在配置文件中设置[move.request.cmd]值。");
        }
        return moveCmdKey;
    }

    /**
     * 根据名称取适配器，
     * @param name
     * @return
     */
    public static RobotCommAdapter getAdapter(String name) {
        return RobotContext.getAdapter(name);
    }

    /**
     * 大杀器----TCS的对象服务器
     */
    public static TCSObjectService getOpenTcsObjectService() {
        return Optional.ofNullable(RobotContext.getTCSObjectService()).orElseThrow(NullPointerException::new);
    }

    /***
     * 根据线名称取openTCS线路图上的车辆
     * @param vehicleName 车辆名称
     */
    public static Vehicle getVehicle(String vehicleName) {
        java.util.Objects.requireNonNull(vehicleName, "车辆名称不能为空");
        return getOpenTcsObjectService().fetchObject(Vehicle.class, vehicleName);
    }

    /***
     * 根据点名称取openTCS线路图上的点
     * @param pointName 点名称
     */
    public static Point getPoint(String pointName) {
        java.util.Objects.requireNonNull(pointName, "点名称不能为空");
        return getOpenTcsObjectService().fetchObject(Point.class, pointName);
    }

    /***
     * 根据点名称取openTCS线路图上的路径
     * @param pathName 路径名称
     */
    public static Path getPath(String pathName) {
        java.util.Objects.requireNonNull(pathName, "路径名称不能为空");
        return getOpenTcsObjectService().fetchObject(Path.class, pathName);
    }

    /***
     * 根据线名称取openTCS线路图上的车辆
     */
    public static Location getLocation(String locationName) {
        java.util.Objects.requireNonNull(locationName, "位置名称不能为空且必须唯一");
        return getOpenTcsObjectService().fetchObject(Location.class, locationName);
    }

    /**
     * 取网络渠道类型，分别为TCP/UDP/RXTX
     *
     * @return
     */
    public static NetChannelType getNetChannelType() {
        String typeString = SettingUtil.getString("net.channel.type", "UDP");
        try {
            return NetChannelType.valueOf(typeString.toUpperCase());
        } catch (Exception e) {
            LOG.error("根据{}取网络渠道类型时出错:{}, 返回 UDP 模式", typeString, e.getMessage());
            return NetChannelType.UDP;
        }
    }

    /***
     * 取运行类型，默认为server
     * @return
     */
    public static String getRunType() {
        return SettingUtil.getString("run.type", "server").toUpperCase();
    }

    /***
     * 以服务器方式启动时的地址
     * @return
     */
    public static String getServerHost() {
        if (NetChannelType.TCP.equals(getNetChannelType()) ||
                NetChannelType.UDP.equals(getNetChannelType())) {
            return SettingUtil.getString("host", "net", "0.0.0.0");
        } else if (NetChannelType.RXTX.equals(getNetChannelType())) {
            return SettingUtil.getString("name", "serialport", "COM6");
        }
        return "";
    }

    /***
     * 以服务器方式启动时的端口
     * @return
     */
    public static Integer getServerPort() {
        if (NetChannelType.TCP.equals(getNetChannelType()) ||
                NetChannelType.UDP.equals(getNetChannelType())) {
            return SettingUtil.getInt("port", "net", 9090);
        } else if (NetChannelType.RXTX.equals(getNetChannelType())) {
            return SettingUtil.getInt("baudrate", "serialport", 38400);
        }
        return 0;
    }

    /***
     * 以客户端方式启动时的地址
     * @param vehicleName 车辆名称
     * @return
     */
    public static String getHost(String vehicleName) {
        if (NetChannelType.TCP.equals(getNetChannelType()) ||
                NetChannelType.UDP.equals(getNetChannelType())) {
            if (RunType.SERVER.name().equalsIgnoreCase(getRunType())) {
                return getServerHost();
            } else {
                return RobotContext.getAdapter(vehicleName).getProcessModel().getVehicleHost();
            }
        } else if (NetChannelType.RXTX.equals(getNetChannelType())) {
            return SettingUtil.getString("name", "serialport", "COM6");
        }
        return "";
    }

    /***
     * 以客户端方式启动时的端口
     * @param vehicleName 车辆名称
     * @return
     */
    public static Integer getPort(String vehicleName) {
        if (NetChannelType.TCP.equals(getNetChannelType()) ||
                NetChannelType.UDP.equals(getNetChannelType())) {
            if (RunType.SERVER.name().equalsIgnoreCase(getRunType())) {
                return getServerPort();
            } else {
                return RobotContext.getAdapter(vehicleName).getProcessModel().getVehiclePort();
            }
        } else if (NetChannelType.RXTX.equals(getNetChannelType())) {
            return SettingUtil.getInt("baudrate", "serialport", 38400);
        }
        return 0;
    }

    /**
     * 车辆状态
     */
    public static Vehicle.State translateVehicleState(OperatingState operationState) {
        switch (operationState) {
            case IDLE:
                return Vehicle.State.IDLE;
            case MOVING:
            case ACTING:
                return Vehicle.State.EXECUTING;
            case CHARGING:
                return Vehicle.State.CHARGING;
            case ERROR:
                return Vehicle.State.ERROR;
            default:
                return Vehicle.State.UNKNOWN;
        }
    }

    /***
     * 是否包含指定的动作名称
     * @param operation
     * @return
     */
    public static boolean isContainActionsKey(String operation) {
        if (ToolsKit.isEmpty(operation)) {
            throw new RuntimeException("动作名称不能为空");
        }
        return RouteHelper.getActionRouteMap().containsKey(operation);
    }

    /**
     * 根据动作名称取工站动作指令集对象
     *
     * @param operation 工站动作名称
     * @return
     */
    public static IAction getLocationAction(String operation) {
        if (ToolsKit.isEmpty(operation)) {
            throw new RuntimeException("动作名称不能为空");
        }
        Route route = RouteHelper.getActionRouteMap().get(operation);
        if (ToolsKit.isNotEmpty(route)) {
            Object routeObj = route.getServiceObj();
            if (ToolsKit.isNotEmpty(routeObj) && routeObj instanceof IAction) {
                return (IAction) routeObj;
            }
        }
        return null;
    }

    /**
     * 是否移动请求
     *
     * @param request 请求对象
     * @return
     */
    public static boolean isMoveRequest(IRequest request) {
        return ReqType.MOVE.equals(request.getReqType());
    }

    /**
     * 是否工站动作请求
     *
     * @param request 请求对象
     * @return
     */
    public static boolean isActionRequest(IRequest request) {
        return ReqType.ACTION.equals(request.getReqType());
    }

    /**
     * 是否业务请求
     *
     * @param request 请求对象
     * @return
     */
    public static boolean isBusinessRequest(IRequest request) {
        return ReqType.BUSINESS.equals(request.getReqType());
    }

    /**
     * 取车辆id
     *
     * @param key 设备id或车辆id或工站key
     * @return 车辆id
     */
    private static Map<String, String> VEHICLE_KEY_MAP = new HashMap<>();
    //查询一个不存在的actionKey，让缓存加载
    private static final String I_LOVE_LAOTANG = "i_love_laotang";

    public static String getVehicleId(String key) {
        String vehicleId = VEHICLE_KEY_MAP.get(key);
        if (ToolsKit.isNotEmpty(vehicleId)) {
            return vehicleId;
        }
        getActionKey(I_LOVE_LAOTANG);
        Set<Vehicle> vehicleSet = RobotContext.getTCSObjectService().fetchObjects(Vehicle.class);
        for (Vehicle vehicle : vehicleSet) {
            VEHICLE_KEY_MAP.put(vehicle.getName(), vehicle.getName());
        }
        return VEHICLE_KEY_MAP.get(key);
    }

    /**
     * 取设备id
     *
     * @param key 设备id或车辆id或工站key
     * @return 设备id
     */
    private static Map<String, String> DEVICE_KEY_MAP = new HashMap<>();

    public static String getDeviceId(String key) {
        String deviceId = DEVICE_KEY_MAP.get(key);
        if (ToolsKit.isNotEmpty(deviceId)) {
            return deviceId;
        }
        getActionKey(I_LOVE_LAOTANG);
        return DEVICE_KEY_MAP.get(key);
    }

    /**
     * 取工站名称
     *
     * @param deviceId 车辆或设备ID标识符或工站名称
     * @return 工站名称
     */
    private static Map<String, Set<String>> ACTION_KEY_MAP = new HashMap<>();

    public static Set<String> getActionKey(String deviceId) {
        if (ToolsKit.isEmpty(deviceId)) {
            throw new RobotException("取工站命称时，设备或车辆标识ID不能为空");
        }
        Set<String> actionKey = ACTION_KEY_MAP.get(deviceId);
        if (ToolsKit.isNotEmpty(actionKey)) {
            return actionKey;
        }
        Map<String, Route> actionRouteMap = RouteHelper.getActionRouteMap();
        if (ToolsKit.isEmpty(actionRouteMap)) {
            LOG.debug("根据[{}]没找到动作指令对象，请确保在动作指令类添加@Action注解", deviceId);
            return null;
        }
        for (Iterator<Map.Entry<String, Route>> iterator = RouteHelper.getActionRouteMap().entrySet().iterator(); iterator.hasNext(); ) {
            Map.Entry<String, Route> entry = iterator.next();
            Route route = entry.getValue();
            Object obj = route.getServiceObj();
            if (!(obj instanceof IAction)) {
                continue;
            }
            IAction action = (IAction) obj;
            String deviceName = action.deviceId();
            String vehicleName = action.vehicleId();
            String actionName = action.actionKey();

            // 将设备id，车辆id, 工站key设置到Map，根据上述的标识符取车辆名称
            VEHICLE_KEY_MAP.put(deviceName, vehicleName);
            VEHICLE_KEY_MAP.put(vehicleName, vehicleName);
            VEHICLE_KEY_MAP.put(actionName, vehicleName);

            // 将设备id，车辆id, 工站key设置到Map，根据上述的标识符取工站名称
            putDeviceName2Set(deviceName, actionName);
            putDeviceName2Set(vehicleName, actionName);
            putDeviceName2Set(actionName, actionName);

            // 将设备id，车辆id, 工站key设置到Map，根据上述的标识符取设备名称
            DEVICE_KEY_MAP.put(deviceName, deviceName);
            DEVICE_KEY_MAP.put(vehicleName, deviceName);
            DEVICE_KEY_MAP.put(actionName, deviceName);
        }
        return ACTION_KEY_MAP.get(deviceId);
    }

    /**
     * 将设备id，车辆id, 工站key设置到Map，根据上述的deviceName取工站名称
     *
     * @param deviceName 设备或车辆id
     * @param actionName 工站动作名称
     * @return
     */
    private static void putDeviceName2Set(String deviceName, String actionName) {
        Set<String> deviceNameSet = ACTION_KEY_MAP.get(deviceName);
        if (ToolsKit.isEmpty(deviceNameSet)) {
            deviceNameSet = new HashSet<>();
        }
        deviceNameSet.add(actionName);
        ACTION_KEY_MAP.put(deviceName, deviceNameSet);
    }

}
