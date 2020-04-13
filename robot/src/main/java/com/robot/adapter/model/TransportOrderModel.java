package com.robot.adapter.model;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.IdUtil;
import com.robot.adapter.constants.RobotConstants;
import com.robot.utils.ElementKit;
import com.robot.utils.RobotUtil;
import com.robot.utils.ToolsKit;
import org.opentcs.data.TCSObjectReference;
import org.opentcs.data.model.Point;
import org.opentcs.data.order.Route;

import java.util.*;

/**
 * 订单信息
 *
 * @author Laotang
 */
public class TransportOrderModel implements java.io.Serializable {

    /**
     * 该对象ID，唯一，要与订单ID区分
     */
    private String id;

    /**
     * 订单ID
     */
    private String orderId;
    /**
     * 订单的最终位置
     */
    private String finalPosition;
    /**
     * 订单的最终工站
     */
    private String finalLocation;
    /**
     * 订单在最终位置上执行的动作
     */
    private String finalOperation;
    /**
     * 该订单与关联的车辆名称
     */
    private String vehicleName;

    private Queue<Route.Step> routeStep;

    private List<String> pointPaths;
    private String pointPathStr;
    private List<LocationOperation> locationOperationList;

    private TransportOrderModel(String vehicleName, String finalPosition, String finalLocation, String finalOperation, List<String> pointPaths,
                                List<LocationOperation> locationOperationList) {
        this.id = IdUtil.objectId();
        this.vehicleName = vehicleName;
        this.finalPosition = finalPosition;
        this.finalLocation = finalLocation;
        this.finalOperation = finalOperation;
        this.pointPaths = pointPaths;
        if (null != pointPaths) {
            this.pointPathStr = CollectionUtil.join(pointPaths.iterator(), ",");
        }
        this.locationOperationList = locationOperationList;
    }

    public String getId() {
        return id;
    }

    public String getFinalPosition() {
        return finalPosition;
    }

    public String getFinalLocation() {
        return finalLocation;
    }

    public String getFinalOperation() {
        return finalOperation;
    }

    public String getVehicleName() {
        return vehicleName;
    }

    public void setVehicleName(String vehicleName) {
        this.vehicleName = vehicleName;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public List<String> getPointPathList() {
        return pointPaths;
    }

    public String getPointPathStr() {
        return pointPathStr;
    }

    public List<LocationOperation> getLocationOperationList() {
        return locationOperationList;
    }

    public static class Builder {
        private String vehicleName;
        private String finalPosition;
        private String finalLocation;
        private String finalOperation;
        private List<String> pointPaths;
        private List<LocationOperation> locationOperationList;

        public Builder() {
        }

        public Builder vehicleName(String vehicleName){
            this.vehicleName = vehicleName;
            return this;
        }

        public Builder destPosition(String destPosition){
            this.finalPosition = destPosition;
            if (null == finalLocation) {
                try {
                    destLocation(ElementKit.duang().point(destPosition).getLocationName());
                } catch (Exception e) {
                    destLocation(destPosition);
                }
            }
            return this;
        }

        public Builder destLocation(String destLocation){
            this.finalLocation = destLocation;
            return this;
        }

        public Builder destLocations(List<LocationOperation> dtoList) {
            locationOperationList = new ArrayList<>();
            locationOperationList.addAll(dtoList);
            return this;
        }

        public Builder finalOperation(String finalOperation){
            this.finalOperation = finalOperation;
            return this;
        }

        public Builder pointPaths(List<String> pointPaths) {
            this.pointPaths = pointPaths;
            return this;
        }


        private void setFinalOperation(String finalPosition) {
            try {
                if (null != RobotUtil.getPoint(finalPosition)) {
                    finalOperation = RobotConstants.OP_MOVE;
                }
            } catch (Exception e) {}

            if ( null == finalOperation ) {
                finalOperation = RobotConstants.OP_NOP;
            }
        }

        public TransportOrderModel build() {
            setFinalOperation(finalPosition);
            return new TransportOrderModel(vehicleName, finalPosition, finalLocation, finalOperation, pointPaths, locationOperationList);
        }
    }

    public Queue<org.opentcs.data.order.Route.Step> getRouteStep() {
        if (null == routeStep) {
            String startPosition = RobotUtil.getAdapter(vehicleName).getProcessModel().getVehiclePosition();

            try {
                RobotUtil.getPoint(finalPosition).getName();
            } catch (Exception e) {
                Set<TCSObjectReference<Point>> pointSet = RobotUtil.getPointByLocationName(finalLocation);
                if (ToolsKit.isNotEmpty(pointSet)) {
                    finalPosition = pointSet.iterator().next().getName();
                }
            }

            if (ToolsKit.isEmpty(finalPosition)) {
                throw new NullPointerException("最终位置点或最终工站没有设置");
            }
            routeStep = new LinkedList<>(RobotUtil.getRoute(vehicleName, startPosition, finalPosition));
        }
        return routeStep;
    }
}
