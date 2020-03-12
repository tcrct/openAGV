package com.robot.adapter.model;

import com.robot.adapter.enumes.OperatingState;
import com.robot.mvc.core.interfaces.IProtocol;

/**
 * 状态模型，更新工厂概述里的UI显示
 * Created by laotang on 2020/2/1.
 */
public class RobotStateModel implements java.io.Serializable {
    /**
     * 当前卡号
     */
    private String currentPosition;
    /**
     * 当前指令名称
     */
    private String cmdKey;
    /**
     * 当前状态
     */
    private OperatingState operatingState;
    /**
     * 协议对象
     */
    private IProtocol protocol;

    public RobotStateModel(IProtocol protocol) {
        this("", protocol);
    }
    public RobotStateModel(String currentPosition, IProtocol protocol) {
        this.currentPosition = currentPosition;
        this.cmdKey = protocol.getCmdKey();
        this.operatingState = OperatingState.MOVING;
        this.protocol = protocol;
    }

    public String getCurrentPosition() {
        return currentPosition;
    }

    public String getCmdKey() {
        return cmdKey;
    }

    public OperatingState getOperatingState() {
        return operatingState;
    }

    public IProtocol getProtocol() {
        return protocol;
    }
}
