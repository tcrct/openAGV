package com.robot.mvc.core.telegram;

import com.robot.mvc.core.interfaces.IResponse;

/**
 * 发送电报接口
 *
 * @author Laotang
 */
public interface ITelegramSender {

    /**
     * 发送报文
     *
     * @param request The {@link Request} to be sent.
     */
    void sendTelegram(IResponse response);
}
