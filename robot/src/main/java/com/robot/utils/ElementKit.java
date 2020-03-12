package com.robot.utils;

import com.robot.adapter.model.ElementModel;
import com.robot.adapter.model.VehiclePointModel;
import com.robot.mvc.core.exceptions.RobotException;
import org.opentcs.data.TCSObjectReference;
import org.opentcs.data.model.Location;
import org.opentcs.data.model.Path;
import org.opentcs.data.model.Point;
import org.opentcs.data.order.Route;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Opentcs元素工具类
 * 包括 Point, Path, Location,Vehicle等
 *
 * @author Laotang
 * @since 1.0
 * @date 2020-03-10
 */
public class ElementKit {

    private static final Logger LOG = LoggerFactory.getLogger(ElementKit.class);
    /**
     * 点名称为key
     */
    private static final Map<String, ElementModel> POINT_ELEMENT_MODEL_MAP = new HashMap<>();
    /**要取得信息的点名称*/
    private String pointName;
    /**当前车辆名称*/
    private String vehicleName;


    private static class InstanceHolder {
        private static final ElementKit INSTANCE = new ElementKit();
    }
    private ElementKit() {
        init();
    }
    public static ElementKit duang() {
        return InstanceHolder.INSTANCE;
    }

    private void init(){
        // 取出地图上所有的点，并设置与该点相关的所有出边点与工站名称
        Set<Point> pointSet =  RobotUtil.getOpenTcsObjectService().fetchObjects(Point.class);
        for (Point point : pointSet) {
            String name = point.getName();
            POINT_ELEMENT_MODEL_MAP.put(name, new ElementModel(name, initIncomingPaths(point), initOutgoingPoints(point), initLocation(point)));
        }
    }

    private Set<String> initIncomingPaths(Point point) {
        Set<TCSObjectReference<Path>> incomingPaths =point.getIncomingPaths();
        if (ToolsKit.isEmpty(incomingPaths)) {
            return null;
        }
        Set<String> incomingPointNamePaths = new HashSet<>(incomingPaths.size());
        if (ToolsKit.isNotEmpty(incomingPaths)) {
            for (TCSObjectReference<Path> outPath : incomingPaths) {
                Path path = RobotUtil.getPath(outPath.getName());
                if (null != path) {
                    TCSObjectReference<Point> destPointReference = path.getDestinationPoint();
                    if (null != destPointReference) {
                        incomingPointNamePaths.add(destPointReference.getName());
                    }
                }
            }
        }
        return incomingPointNamePaths;
    }

    private Set<String> initOutgoingPoints(Point point) {
        Set<TCSObjectReference<Path>> outgoingPaths =point.getOutgoingPaths();
        if (ToolsKit.isEmpty(outgoingPaths)) {
            return null;
        }
        Set<String> outgoPointNameSet = new HashSet<>(outgoingPaths.size());
        if (ToolsKit.isNotEmpty(outgoingPaths)) {
            for (TCSObjectReference<Path> outPath : outgoingPaths) {
                Path path = RobotUtil.getPath(outPath.getName());
                if (null != path) {
                    TCSObjectReference<Point> destPointReference = path.getDestinationPoint();
                    if (null != destPointReference) {
                        outgoPointNameSet.add(destPointReference.getName());
                    }
                }
            }
        }
        return outgoPointNameSet;
    }

    private Set<String> initLocation(Point point) {
        Set<Location.Link> locationLinkSet = point.getAttachedLinks();
        if (ToolsKit.isEmpty(locationLinkSet)) {
            return new HashSet<>(1);
        }
        Set<String> locationLinkNameSet = new HashSet<>(locationLinkSet.size());
        for (Location.Link link : locationLinkSet) {
            if (null != link) {
                TCSObjectReference<Location> locationReference = link.getLocation();
                if (null != locationReference) {
                    locationLinkNameSet.add(locationReference.getName());
                }
            }
        }
        return locationLinkNameSet;
    }

    /**
     *将部份路由信息转为VehiclePointModel
     * ElementKit.duang().setRout()
     * @param routeList
     */
    public void route(List<Route.Step> routeList) {
        for (Route.Step step : routeList) {
            Point sourcePoint = step.getSourcePoint();
            Point destinationPoint = step.getDestinationPoint();
            if (null != sourcePoint && null != destinationPoint) {
                ElementModel model =getModel(sourcePoint.getName());
                if (null != model) {
                    VehiclePointModel vehiclePointModel = new VehiclePointModel();
                    vehiclePointModel.setVehicleName(vehicleName);
                    vehiclePointModel.setNextPointName(destinationPoint.getName());
                    vehiclePointModel.setCurrentPointName(sourcePoint.getName());
                    model.setVehiclePointModel(vehiclePointModel);
                }
            }
        }
    }


    private ElementModel getModel(String pointName) {
        if (ToolsKit.isEmpty(pointName)) {
            throw new RobotException("点名称为空，请先设置点名称！");
        }
        ElementModel model = POINT_ELEMENT_MODEL_MAP.get(pointName);
        if (null == model) {
            throw new RobotException("根据["+pointName+"]找不到对应的PointElementModel，请确保点名称正确！");
        }
        return model;
    }

    /**
     * 设置点名称
     * @param pointName 点名称
     * @return
     */
    public ElementKit point(String pointName) {
        this.pointName = pointName;
        return this;
    }

    public ElementKit vehicle(String vehicleName) {
        this.vehicleName = vehicleName;
        return this;
    }

    /**
     * 取出该点的所有出边点
     * @return
     */
    public Set<String> getOutPointNames() {
        Set<String> outPointSet = getModel(pointName).getOutPointNameSet();
        if (ToolsKit.isEmpty(outPointSet)){
            LOG.info("该[{}]点没有出边点！", pointName);
        }
        return outPointSet;
    }

    /**
     * 取出该点对应的工作站集合
     * @return
     */
    public Set<String> getLocationNames() {
        Set<String> locationNameSet = getModel(pointName).getLocationNameSet();
        if (ToolsKit.isEmpty(locationNameSet)){
            LOG.info("该[{}]点没有设置工作站！", pointName);
        }
        return locationNameSet;
    }


    public String getLocationName() {
        Set<String> tempSet =  getModel(pointName).getLocationNameSet();
        if (ToolsKit.isEmpty(tempSet)) {
            return "";
        }
        return tempSet.iterator().next();
    }
    /**
     * 取出当前车辆在当前点的下一个点，是根据Route路径决定的下一个点
     * ElementKit.duang().point("1").vehicle("A001").getNextPointName();
     * @return
     */
    public String getNextPointName() {
        if (ToolsKit.isEmpty(vehicleName)) {
            throw new RobotException("车辆名称为空，请先设置车辆名称！");
        }
        VehiclePointModel vehiclePointModel = getModel(pointName).getVehiclePointModelMap().get(pointName);
        if (ToolsKit.isEmpty(vehiclePointModel)) {
            LOG.info("车辆[{}]]取不到当前点[{}]的下一个点，可能下一个点是终点，请检查！", vehicleName, pointName);
            return null;
        }
        return vehiclePointModel.getNextPointName();
    }

}
