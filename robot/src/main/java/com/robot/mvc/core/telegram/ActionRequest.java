package com.robot.mvc.core.telegram;

import com.robot.mvc.core.enums.ReqType;
import com.robot.mvc.core.interfaces.IActionCommand;
import com.robot.mvc.core.interfaces.IProtocol;

/**
 * 工作站动作请求
 * 当车辆到达指定位置后，对工作站发起的一连串动作指令请求
 * 包括发送请求，等待响应回复
 * <p>
 * Created by laotang on 2020/1/12.
 */
public abstract class ActionRequest extends BaseRequest implements IActionCommand {

    /**
     * 用于ActionsQueue队列中，标记该动作请求的下标元素位置
     */
    private double index;
    /**
     * 车辆ID
     */
    private String vehicleId;

    public ActionRequest(IProtocol protocol) {
        super(ReqType.ACTION, protocol);
        super.setNeedSend(true);
    }

    public abstract String cmd();

    public double getIndex() {
        return index;
    }

    public void setIndex(double index) {
        this.index = index;
    }

    public String getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(String vehicleId) {
        this.vehicleId = vehicleId;
    }
}
