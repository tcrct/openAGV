package org.opentcs.kernel.extensions.servicewebapi.console.interfaces;

import java.io.IOException;
import java.util.List;

/**
 * WebSocket接口
 * Created by laotang on 2020/3/27.
 */
public interface IWebSocket {

    /**
     *  推送消息到指定订阅者
     * @param userIds 接收人
     * @param message 推送的消息
     * @throws IOException
     */
    void push(List<String> userIds, String message);

    /**
     * 推送消息到所有订阅者
     * @param message
     * @throws IOException
     */
    void push(String message);

}
