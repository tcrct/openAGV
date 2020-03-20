package com.robot.adapter.model;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.IdUtil;
import com.robot.adapter.constants.RobotConstants;
import com.robot.utils.ElementKit;
import com.robot.utils.RobotUtil;
import org.opentcs.data.order.Route;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

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

    private TransportOrderModel(String vehicleName, String finalPosition, String finalLocation, String finalOperation, List<String> pointPaths) {
        this.id = IdUtil.objectId();
        this.vehicleName = vehicleName;
        this.finalPosition = finalPosition;
        this.finalLocation = finalLocation;
        this.finalOperation = finalOperation;
        this.pointPaths = pointPaths;
        this.pointPathStr = CollectionUtil.join(pointPaths.iterator(), ",");
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

    public static class Builder {
        private String vehicleName;
        private String finalPosition;
        private String finalLocation;
        private String finalOperation;
        private List<String> pointPaths;

        public Builder() {}

        public Builder vehicleName(String vehicleName){
            this.vehicleName = vehicleName;
            return this;
        }

        public Builder destPosition(String destPosition){
            this.finalPosition = destPosition;
            if (null == finalLocation) {
                finalLocation = ElementKit.duang().point(destPosition).getLocationName();
            }
            return this;
        }

        public Builder destLocation(String destLocation){
            this.finalLocation = destLocation;
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
            return new TransportOrderModel(vehicleName, finalPosition, finalLocation, finalOperation, pointPaths);
        }
    }

    public Queue<org.opentcs.data.order.Route.Step> getRouteStep() {
        if (null == routeStep) {
            String startPosition = RobotUtil.getAdapter(vehicleName).getProcessModel().getVehiclePosition();
            routeStep = new LinkedList<>(RobotUtil.getRoute(vehicleName, startPosition, finalPosition));
        }
        return routeStep;
    }
}
