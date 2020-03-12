package org.opentcs.event;

import org.opentcs.data.order.TransportOrder;

/**
 * 自定义移动订单完成回调
 * 调用适配器的processMessage方法，通知该订单的状态
 *
 * @author Laotang
 * @date 2020-03-12
 */
public class RobotTransportOrderCallBack implements java.io.Serializable {

    private TransportOrder transportOrder;
    private TransportOrder.State state;

    public RobotTransportOrderCallBack(TransportOrder transportOrder, TransportOrder.State state) {
        this.transportOrder = transportOrder;
        this.state = state;
    }

    public TransportOrder getTransportOrder() {
        return transportOrder;
    }

    public TransportOrder.State getState() {
        return state;
    }
}
