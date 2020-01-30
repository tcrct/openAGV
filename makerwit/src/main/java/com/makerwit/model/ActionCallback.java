package com.makerwit.model;

import com.robot.mvc.core.interfaces.IActionCallback;

/**
 * Created by laotang on 2020/1/30.
 */
public class ActionCallback implements java.io.Serializable {

    /**
     * 请求ID
     */
    private String id;
    /**
     * 车辆ID
     */
    private String vehicleId;
    /**
     * 握手验证码
     */
    private String code;
    /**
     * 工站动作名称
     */
    private String actionKey;
    /**
     * 回调事件
     */
    private IActionCallback callback;


    public ActionCallback(String id, String vehicleId, String code, String actionKey, IActionCallback callback) {
        this.id = id;
        this.vehicleId = vehicleId;
        this.code = code;
        this.actionKey = actionKey;
        this.callback = callback;
    }

    public String getId() {
        return id;
    }

    public String getVehicleId() {
        return vehicleId;
    }

    public String getCode() {
        return code;
    }

    public String getActionKey() {
        return actionKey;
    }

    public IActionCallback getCallback() {
        return callback;
    }
}
