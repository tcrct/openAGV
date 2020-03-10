package com.robot.adapter.model;

import java.util.HashSet;
import java.util.Set;

/**
 * 车辆与点的关系
 *
 * @author Laotang
 * @since 1.0
 * @date 2020-03-10
 */
public class VehiclePointModel implements java.io.Serializable {

    /**车辆名称*/
    private String vehicleName;
    /**车辆会经过的所有点*/
    private Set<String> pointNames = new HashSet<>();
    /**车辆当前所在的点，包括停留在上面，刚刚经过的点名称，未到达下一个点时的名称*/
    private String currentPointName;
    /**根据行驶路径，在当前点的下一个点名称*/
    private String nextPointName;

    public VehiclePointModel() {
    }

    public String getVehicleName() {
        return vehicleName;
    }

    public void setVehicleName(String vehicleName) {
        this.vehicleName = vehicleName;
    }

    public Set<String> getPointNames() {
        return pointNames;
    }

    public void setPointNames(Set<String> pointNames) {
        this.pointNames = pointNames;
    }

    public String getCurrentPointName() {
        return currentPointName;
    }

    public void setCurrentPointName(String currentPointName) {
        this.currentPointName = currentPointName;
    }

    public String getNextPointName() {
        return nextPointName;
    }

    public void setNextPointName(String nextPointName) {
        this.nextPointName = nextPointName;
    }
}
