package com.openagv.core.interfaces;

import com.openagv.core.AgvResult;

public interface ITelegram {

    /**
     * 接收到消息
     *
     * @return  AgvResult 发送/广播的消息
     */
    AgvResult handle(Object telegramObj);

}
