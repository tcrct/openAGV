package com.robot.adapter.model;

import com.robot.utils.RobotUtil;

import java.util.*;

/**
 * 车辆，设备，工站名称对象
 *
 * @author Laotang
 */
public class EntryName implements java.io.Serializable {

    private static final String EMPTY_KEY = "I_LOVE_LAOTANG";
    /**
     * 车辆名称
     */
    private Set<String> vehicleNameSet = new HashSet<>();
    /**
     * 工站动作名称
     */
    private Set<String> actionNameSet = new HashSet<>();

    /**
     * 设备(工站)名称
     */
    private Set<String> deviceNameSet = new HashSet<>();

    private List<String> vehicleNameList;
    private List<String> actionNameList;
    private List<String> deviceNameList;

    public EntryName() {

    }

    public EntryName(Set<String> vehicleNameSet, Set<String> actionNameSet, Set<String> deviceNameSet) {
        this.vehicleNameSet = vehicleNameSet;
        this.actionNameSet = actionNameSet;
        this.deviceNameSet = deviceNameSet;
    }

    public List<String> getVehicleNameList() {
        if (null == vehicleNameList) {
            RobotUtil.getEntryName(EMPTY_KEY);
            vehicleNameList = new ArrayList<>(vehicleNameSet);
        }
        return  vehicleNameList;
    }

    public List<String> getActionNameList() {
        if (null == actionNameList) {
            RobotUtil.getEntryName(EMPTY_KEY);
            actionNameList = new ArrayList<>(actionNameSet);
        }
        return actionNameList;
    }

    public List<String> getDeviceNameList() {
        if (null == deviceNameList) {
            RobotUtil.getEntryName(EMPTY_KEY);
            deviceNameList = new ArrayList<>(deviceNameSet);
        }
        return deviceNameList;
    }

    public void setVehicleName2Set(String vehicleName) {
        vehicleNameSet.add(vehicleName);
    }
    public void setDeviceName2Set(String deviceName) {
        deviceNameSet.add(deviceName);
    }
    public void setActionName2Set(String actionName) {
        actionNameSet.add(actionName);
    }
}
