package com.robot.adapter.model;

import org.opentcs.data.order.Route;

import java.util.Queue;

/**
 * 订单信息
 *
 * @author Laotang
 */
public class TransportOrderModel implements java.io.Serializable {

    /**
     * 订单ID
     */
    private String orderId;
    /**
     * 订单的最终位置
     */
    private String finalPosition;
    /**
     * 订单在最终位置上执行的动作
     */
    private String operation;
    /**
     * 该订单与关联的车辆名称
     */
    private String vehicleName;

    private Queue<Route.Step> routeStep;

    public TransportOrderModel() {
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getFinalPosition() {
        return finalPosition;
    }

    public void setFinalPosition(String finalPosition) {
        this.finalPosition = finalPosition;
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public String getVehicleName() {
        return vehicleName;
    }

    public void setVehicleName(String vehicleName) {
        this.vehicleName = vehicleName;
    }

    public Queue<org.opentcs.data.order.Route.Step> getRouteStep() {
        return routeStep;
    }

    public void setRouteStep(Queue<org.opentcs.data.order.Route.Step> routeStep) {
        this.routeStep = routeStep;
    }
}
