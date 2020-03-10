package com.robot.adapter.model;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * 封装点，车辆，工站的关联
 *
 * @author Laotang
 * @since 1.0
 * @date 2020-03-10
 */
public class PointElementModel implements java.io.Serializable {

    /**当前点名称*/
    private String pointName;
    /**出边点名称集合，即链接线对端的点*/
    private Set<String> inPointNameSet = new HashSet();
    /**出边点名称集合，即链接线对端的点*/
    private Set<String> outPointNameSet = new HashSet();
    /**工站名称集合*/
    private Set<String> locationNameSet = new HashSet<>();
   /**车辆与当前点的关联*/
    private Map<String, VehiclePointModel> vehiclePointModelMap = new HashMap<>();

    public PointElementModel() {
    }

    public PointElementModel(String pointName, Set<String> inPointNameSet, Set<String> outPointNameSet, Set<String> locationNameSet) {
        this.pointName = pointName;
        this.inPointNameSet = inPointNameSet;
        this.outPointNameSet = outPointNameSet;
        this.locationNameSet = locationNameSet;
    }

    public String getPointName() {
        return pointName;
    }

    public void setPointName(String pointName) {
        this.pointName = pointName;
    }

    public Set<String> getInPointNameSet() {
        return inPointNameSet;
    }

    public void setInPointNameSet(Set<String> inPointNameSet) {
        this.inPointNameSet = inPointNameSet;
    }

    public Set<String> getOutPointNameSet() {
        return outPointNameSet;
    }

    public void setOutPointNameSet(Set<String> outPointNameSet) {
        this.outPointNameSet = outPointNameSet;
    }

    public Set<String> getLocationNameSet() {
        return locationNameSet;
    }

    public void setLocationNameSet(Set<String> locationNameSet) {
        this.locationNameSet = locationNameSet;
    }

    public Map<String, VehiclePointModel> getVehiclePointModelMap() {
        return vehiclePointModelMap;
    }

    public void setVehiclePointModel(VehiclePointModel vehiclePointModel) {
        this.vehiclePointModelMap.put(vehiclePointModel.getCurrentPointName(), vehiclePointModel);
    }
}
