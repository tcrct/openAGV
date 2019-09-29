package com.openagv.core.interfaces;

import com.openagv.opentcs.telegrams.OrderRequest;

import java.util.List;

public interface ITelegram {

    /**
     * 接收到消息
     *
     * @return  AgvResult数组 发送/广播的消息，因为TCP时，可能会一次接收到多个指令
     */
    List<IRequest> handle(OrderRequest telegram);


}
