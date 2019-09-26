package com.openagv.core.interfaces;

import com.openagv.core.AgvResult;
import com.openagv.opentcs.model.ProcessModel;
import org.opentcs.drivers.vehicle.MovementCommand;

public interface ITelegram {

    /**
     * 接收到消息
     *
     * @return  AgvResult 发送/广播的消息
     */
    AgvResult handle(Object telegramObj);

    AgvResult handle(ProcessModel processModel, MovementCommand cmd);

}
