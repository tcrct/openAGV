package com.openagv.dto;

/**
 * 协议指令路径步骤对象
 * 即该车辆需要执行的所有参数
 *
 * Created by laotang on 2019/11/8.
 */
public class PathStepDto implements java.io.Serializable {

    /**点名称*/
    private String pointName;
    /**点对应的动作指令*/
    private String pointAction;
    /**是否已经执行，默认没执行，在上报卡号后，需要更改其状态为true*/
    private Boolean isExceute = false;

    public PathStepDto(String pointName, String pointAction) {
        this.pointName = pointName;
        this.pointAction = pointAction;
    }

    public PathStepDto(String pointName, String pointAction, Boolean isExceute) {
        this.pointName = pointName;
        this.pointAction = pointAction;
        this.isExceute = isExceute;
    }

    public String getPointName() {
        return pointName;
    }

    public String getPointAction() {
        return pointAction;
    }

    public Boolean getExceute() {
        return isExceute;
    }

    /**将值更改为已经执行或操作*/
    public void setExceuteToTrue() {
        isExceute = true;
    }
}
