package com.robot.utils;

import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import com.robot.RobotContext;
import com.robot.adapter.RobotCommAdapter;
import com.robot.adapter.enumes.OperatingState;
import com.robot.adapter.model.EntryName;
import com.robot.contrib.netty.comm.ClientEntry;
import com.robot.contrib.netty.comm.NetChannelType;
import com.robot.contrib.netty.comm.RunType;
import com.robot.mvc.core.enums.ReqType;
import com.robot.mvc.core.exceptions.RobotException;
import com.robot.mvc.core.interfaces.*;
import com.robot.mvc.helpers.RouteHelper;
import com.robot.mvc.main.DispatchFactory;
import com.robot.mvc.model.Route;
import io.netty.channel.Channel;
import org.opentcs.components.kernel.services.TCSObjectService;
import org.opentcs.components.kernel.services.VehicleService;
import org.opentcs.data.TCSObjectReference;
import org.opentcs.data.model.Location;
import org.opentcs.data.model.Path;
import org.opentcs.data.model.Point;
import org.opentcs.data.model.Vehicle;
import org.opentcs.drivers.vehicle.AdapterCommand;
import org.opentcs.strategies.basic.routing.DefaultRouter;

import java.util.*;

/**
 * Created by laotang on 2020/1/22.
 */
public class RobotUtil {

    private static final Log LOG = LogFactory.get();

    /**
     * 协议解码器
     */
    private static IProtocolMatcher protocolMatcher;
    /**
     * 重复发送对象
     */
    private static IRepeatSend repeatSend;

    /**
     * 是否开发模式
     *
     * @return
     */
    public static boolean isDevMode() {
        return SettingUtil.getBoolean("dev.mode", false);
    }


    /**
     * 取所有车辆
     * @return
     */
    public static List<String> getAllVehicleName() {
        return new ArrayList<>(RobotContext.getAdapterMap().keySet());
    }

    public static String getVehicleId(String key) {
        try {
            return RobotUtil.getEntryName(key).getVehicleNameList().get(0);
        } catch (Exception e) {
            LOG.error("可能没有该车辆[{}]对应的业务逻辑服务类，请检查！", key);
            return key;
        }
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
     *
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
     * 根据线名称取openTCS线路图上的位置名称取出位置
     */
    public static Location getLocation(String locationName) {
        java.util.Objects.requireNonNull(locationName, "位置名称不能为空且必须唯一");
        return getOpenTcsObjectService().fetchObject(Location.class, locationName);
    }

    private static NetChannelType netChannelType;

    /**
     * 取网络渠道类型，分别为TCP/UDP/RXTX
     *
     * @return
     */
    public static NetChannelType getNetChannelType() {
        if (null == netChannelType) {
            String typeString = SettingUtil.getString("net.channel.type", "TCP");
            try {
                netChannelType = NetChannelType.valueOf(typeString.toUpperCase());
            } catch (Exception e) {
                LOG.error("根据{}取网络渠道类型时出错:{}, 返回 UDP 模式", typeString, e.getMessage());
                netChannelType = NetChannelType.UDP;
            }
        }
        return netChannelType;
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
            return SettingUtil.getString("server.host", "0.0.0.0");
        } else if (NetChannelType.RXTX.equals(getNetChannelType())) {
            return SettingUtil.getString("rxtx.name", "COM3");
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
            return SettingUtil.getInt("server.port", 7070);
        } else if (NetChannelType.RXTX.equals(getNetChannelType())) {
            return SettingUtil.getInt("rxtx.baudrate", 38400);
        }
        return 0;
    }

    /***
     * 以客户端方式启动时的地址
     * @param vehicleName 车辆名称
     * @return
     */
    public static String getVehicleHost(String vehicleName) {
        if (NetChannelType.TCP.equals(getNetChannelType()) ||
                NetChannelType.UDP.equals(getNetChannelType())) {
            return RobotContext.getAdapter(vehicleName).getProcessModel().getVehicleHost();
        } else if (NetChannelType.RXTX.equals(getNetChannelType())) {
            return SettingUtil.getString("name", "rxtx", "COM3");
        }
        return "";
    }

    /***
     * 以客户端方式启动时的端口
     * @param vehicleName 车辆名称
     * @return
     */
    public static Integer getVehiclePort(String vehicleName) {
        if (NetChannelType.TCP.equals(getNetChannelType()) ||
                NetChannelType.UDP.equals(getNetChannelType())) {
            return RobotContext.getAdapter(vehicleName).getProcessModel().getVehiclePort();
        } else if (NetChannelType.RXTX.equals(getNetChannelType())) {
            return SettingUtil.getInt("baudrate", "rxtx", 38400);
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
     * 是否订单完成请求
     *
     * @param request 请求对象
     * @return
     */
    public static boolean isFinishRequest (IRequest request) {
        return ReqType.FINISH.equals(request.getReqType());
    }

    private static Map<String, EntryName> ENTRYNAME_MAP = new HashMap<>();
    /**
     * 根据车辆，设备，动作名称取EntryName对象
     *
     * @param key 车辆或设备ID标识符或工站名称
     * @return 工站名称
     */
    public static EntryName getEntryName(String key) {
        if (ToolsKit.isEmpty(key)) {
            throw new RobotException("取工站命称时，设备或车辆标识ID不能为空");
        }
        EntryName entryName = ENTRYNAME_MAP.get(key);
        if (ToolsKit.isNotEmpty(entryName)) {
            return entryName;
        }

        if (!ENTRYNAME_MAP.isEmpty()) {
            LOG.info("EntryName Map 已经存在，不需要重复调用");
            return null;
        }

        Map<String, Route> actionRouteMap = RouteHelper.getActionRouteMap();
        if (ToolsKit.isEmpty(actionRouteMap)) {
            throw new RobotException("没找到动作指令路由集合，请确保在动作指令类添加@Action注解");
        }
        for (Iterator<Map.Entry<String, Route>> iterator = actionRouteMap.entrySet().iterator(); iterator.hasNext(); ) {
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
            put2Set(deviceName, vehicleName, deviceName, actionName);
            put2Set(vehicleName, vehicleName, deviceName, actionName);
            put2Set(actionName, vehicleName, deviceName, actionName);
        }

        entryName = ENTRYNAME_MAP.get(key);
        // 如果没有则用车辆协议生成
        if (ToolsKit.isEmpty(entryName)) {
            Map<String, Route> serviceRouteMap = RouteHelper.getServiceRouteMap();
            if (ToolsKit.isEmpty(serviceRouteMap)) {
                throw new RobotException("没找到车辆协议指令路由集合，请确保在车辆协议类添加@Service注解");
            }
            for (Iterator<Map.Entry<String, Route>> iterator = serviceRouteMap.entrySet().iterator(); iterator.hasNext(); ) {
                Map.Entry<String, Route> entry = iterator.next();
                String vehicleName  = entry.getKey();
                Route route = entry.getValue();
                Object obj = route.getServiceObj();
                if (!(obj instanceof IService)) {
                    continue;
                }
                put2Set(vehicleName, vehicleName, null, null);
            }
        }
        return ENTRYNAME_MAP.get(key);
    }

    private static void put2Set(String key, String vehicleName, String deviceName, String actionName) {
        EntryName entryName = ENTRYNAME_MAP.get(key);
        if (ToolsKit.isEmpty(entryName)) {
            entryName = new EntryName();
        }
        entryName.setVehicleName2Set(vehicleName);
        entryName.setDeviceName2Set(deviceName);
        entryName.setActionName2Set(actionName);
        ENTRYNAME_MAP.put(key, entryName);
    }

    /**
     * 是否以客户端的方式运行
     */
    public static boolean isClientRunType() {
        return RunType.CLIENT.name().equalsIgnoreCase(getRunType());
    }

    /**
     * 是否以服务器端的方式运行
     */
    public static boolean isServerRunType() {
        return RunType.SERVER.name().equalsIgnoreCase(getRunType());
    }

    private static final Map<String, String> CLIENT_ENTRY_KEY_MAP = new HashMap<>();

    public static void setClientEntryKey(String name, String host, int port) {
        CLIENT_ENTRY_KEY_MAP.put(name, ClientEntry.createClientEntryKey(host, port));
    }

    public static String getCleintEntryKey(String msg) {
        List<IProtocol> protocolList = RobotContext.getRobotComponents().getProtocolMatcher().encode(msg);
        if (ToolsKit.isEmpty(protocolList)) {
            throw new RobotException("protocolList is null");
        }
        String clientEntryKey = "";
        for (IProtocol protocol : protocolList) {
            clientEntryKey = CLIENT_ENTRY_KEY_MAP.get(protocol.getDeviceId());
        }
        return clientEntryKey;
    }


    /**
     * 将接收到的报文转换为IProtocol对象列表集合
     *
     * @param telegramData
     * @return
     */
    public static List<IProtocol> toProtocolList(String telegramData) {
        if (null == protocolMatcher || null == repeatSend) {
            initComponents();
        }
        return protocolMatcher.encode(telegramData);
    }

    /**
     * 初始化协议解析对象
     */
    private static void initComponents() {

        IComponents agvComponents = RobotContext.getRobotComponents();
        if (ToolsKit.isEmpty(agvComponents)) {
            throw new RobotException("OpenAGV组件对象不能为空,请先实现IComponents接口，并在Duang.java里设置setComponents方法");
        }

        protocolMatcher = agvComponents.getProtocolMatcher();
        if (ToolsKit.isEmpty(protocolMatcher)) {
            throw new RobotException("协议解码器不能为空，请先实现IComponents接口里的getProtocolDecode方法");
        }

        repeatSend = agvComponents.getRepeatSend();
        if (ToolsKit.isEmpty(repeatSend)) {
            throw new RobotException("重复发送不能为空，请先实现IComponents接口里的getRepeatSend方法");
        }
    }

    /**
     * 取报文解析组件
     *
     * @return
     */
    public static IProtocolMatcher getProtocolMatcher() {
        if (null == protocolMatcher) {
            initComponents();
        }
        return protocolMatcher;
    }

    /**
     * 取重发组件
     *
     * @return
     */
    public static IRepeatSend getRepeatSend() {
        if (null == repeatSend) {
            initComponents();
        }
        return repeatSend;
    }

    /**
     * 将接收到的报文转至调度工厂
     *
     * @param channel       netty channel
     * @param clientEntries 客户端实体对象集合
     * @param telegramData  接收到的报文消息
     */
    public static void channelReadToDispatchFactory(Channel channel, Map<String, ClientEntry> clientEntries, String telegramData) {
        // 设置通讯通道到客户端对象
        List<IProtocol> protocolList = RobotUtil.toProtocolList(telegramData);
        if (ToolsKit.isEmpty(protocolList)) {
            LOG.warn("将接收到的报文[{}]转换为List<IProtocol>时，List对象为空！", telegramData);
            return;
        }
        for (IProtocol protocol : protocolList) {
            String key = protocol.getDeviceId();
            if (ToolsKit.isEmpty(key)) {
                LOG.warn("车辆/设备标识符不能为空");
                continue;
            }
            ClientEntry client = clientEntries.get(key);
            if (ToolsKit.isEmpty(client)) {
                LOG.warn("根据车辆/设备标识符[{}]查找不到对应的ClientEntry对象，退出本次访问，请检查！", key);
                continue;
            }
            client.setChannel(channel);
//            LOG.info("netty channel id {}", client.getChannel().id().asLongText());
            DispatchFactory.onIncomingTelegram(protocol);
        }
    }

    /**
     *取车辆由起始点到结束点，所经过的途径。
     * 不受交通管制限制
     *
     * @param vehicleName 车辆名称
     * @param startPointName 起始点
     * @param endPointName 结束点
     * @return
     */
    public static List<org.opentcs.data.order.Route.Step> getRoute(String vehicleName, String startPointName, String endPointName) {
        DefaultRouter router = RobotContext.getAdapter(vehicleName).getRouter();
        // 先更新路由table,取出路由，如果不调用该方法，会抛出空指针异常
        router.initialize();//router.updateRoutingTables();
        Optional<org.opentcs.data.order.Route> optionalRoute = router.getRoute(RobotUtil.getVehicle(vehicleName),
                RobotUtil.getPoint(startPointName),
                RobotUtil.getPoint(endPointName));
        return optionalRoute.isPresent() ? optionalRoute.get().getSteps() : null;
    }

    /**
     * 发送命令
     * @param command 命令
     */
    public static void sendCommAdapterCommand(String vehicleName, AdapterCommand command) {
        try {
            TCSObjectReference<Vehicle> vehicleRef = getVehicle(vehicleName).getReference();
            VehicleService vehicleService = getAdapter(vehicleName).getVehicleService();
            ThreadUtil.execute(new Runnable() {
                @Override
                public void run() {
                    vehicleService.sendCommAdapterCommand(vehicleRef, command);
                }
            });
        } catch (Exception ex) {
            LOG.warn("发送命令[{}]到适配器[{}]时出错：{}", command, vehicleName, ex);
        }
    }

    /**
     * 根据点名称取在该点上的车辆对象
     * @param pointName 点名称
     * @return 如果该点在有车辆，则返回Vehicle对象，否则返回null
     */
    public static Vehicle getVehicleByPoint(String pointName) {
        List<String> vehicleNameList = getAllVehicleName();
        if (ToolsKit.isEmpty(vehicleNameList)) {
            LOG.error("没有找到车辆，车辆列表为空！");
            return null;
        }
        for (String vehicleName : vehicleNameList) {
            String vehiclePosition = RobotUtil.getAdapter(vehicleName).getProcessModel().getVehiclePosition();
            if (vehiclePosition.equals(pointName)) {
                LOG.info("在点[{}]上有车辆[{}]", pointName, vehicleName);
                return RobotUtil.getVehicle(vehicleName);
            }
        }
        return null;
//        String vehicleName = getPoint(pointName).getOccupyingVehicle().getName();
//        return ToolsKit.isEmpty(vehwg j icleName) ? null : RobotUtil.getVehicle(vehicleName);
    }

    /**
     * 根据点取与该点关联的工站对象
     * @param point 点
     * @return 不存在则返回null
     */
    public static Set<TCSObjectReference<Location>> getLocationsByPoint(Point point) {
        Set<Location.Link> links = point.getAttachedLinks();
        if (ToolsKit.isEmpty(links)) {
            return null;
        }
        Set<TCSObjectReference<Location>> locationSet = new HashSet<>();
        for (Location.Link link : links) {
            locationSet.add(link.getLocation());
        }
        return locationSet;
    }

    public static Location getLocationByPointName(String pointName) {
        Set<TCSObjectReference<Location>> set = getLocationsByPoint(RobotUtil.getPoint(pointName));
        String locationName = "";
        for (TCSObjectReference<Location> locationReference : set) {
            locationName = locationReference.getName();
        }
        return RobotUtil.getLocation(locationName);
    }

    /**
     * 根据工站名称取与该工站关联的点对象
     * @param locationName 工作站名称
     * @return
     */
    public static Set<TCSObjectReference<Point>> getPointByLocationName(String locationName){
        Location location = RobotUtil.getLocation(locationName);
        if (ToolsKit.isEmpty(location)) {
            return null;
        }
        Set<Location.Link> links = location.getAttachedLinks();
        if (ToolsKit.isEmpty(links)) {
            return null;
        }
        Set<TCSObjectReference<Point>> pointSet = new HashSet<>();
        for (Location.Link link : links) {
            pointSet.add(link.getPoint());
        }
        return pointSet;
    }
}
