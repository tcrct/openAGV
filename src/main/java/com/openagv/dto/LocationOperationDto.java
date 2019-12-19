package com.openagv.dto;

/***
 * 站点动作Dto
 * 即到达该站点/设备后执行的动作关系
 *
 * @author Laotang
 */
public class LocationOperationDto {

    /**车辆*/
    private String vehicleName;
    /**站点名称*/
    private String location;
    /**站点动作名称*/
    private String operation;

    public LocationOperationDto() {
    }

    public LocationOperationDto(String vehicleName, String location, String operation) {
        this.vehicleName = vehicleName;
        this.location = location;
        this.operation = operation;
    }

    public String getVehicleName() {
        return vehicleName;
    }

    public void setVehicleName(String vehicleName) {
        this.vehicleName = vehicleName;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }
}
