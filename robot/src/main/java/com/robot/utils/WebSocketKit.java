package com.robot.utils;

import com.robot.mvc.core.exceptions.RobotException;
import org.opentcs.kernel.extensions.servicewebapi.console.interfaces.IWebSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WebSocketKit {

    private static final Logger LOG = LoggerFactory.getLogger(WebSocketKit.class);
    /**
     * WebSocket类Map集合，key为类名，如果没有设置则为类全名
     */
    private static Map<String, IWebSocket> WEBSOCKET_MAP = new HashMap<>();

    private static class WebSocketKitHolder {
        private static final WebSocketKit INSTANCE = new WebSocketKit();
    }
    private WebSocketKit() {
    }
    public static final WebSocketKit duang() {
        return WebSocketKitHolder.INSTANCE;
    }
    public static final void putWebSocket(String topic , IWebSocket webSocket) {
        WEBSOCKET_MAP.put(topic, webSocket);
    }
    /*****************************************************************************/
    //主题，即ws的uri部份，如果有值，则所有订阅了该主题的用户都收到推送消息
    private String topic;
    // 可以指定用户接收到推送消息
    private List<String> userIdList;
    // 推送内容
    private String message;

    /**
     * 推送的主题
     * @param topic 推出的主题，主题 必须全局唯一
     * @return
     */
    public WebSocketKit topic(String topic) {
        this.topic = topic;
        return this;
    }

    /**
     * 指定用户
     */
    public WebSocketKit userIds(List<String> userIds) {
        this.userIdList = userIds;
        return this;
    }

    /**
     * 推送内容
     * @param message
     * @return
     */
    public WebSocketKit message(String message) {
        this.message = message;
        return this;
    }

    /**
     * 推送消息到指定的topic里
     * @return
     */
    public boolean push() {
        IWebSocket webSocket = WEBSOCKET_MAP.get(topic);
        if(ToolsKit.isEmpty(webSocket)) {
            throw new NullPointerException("推送消息到["+topic+"]时失败，对应的WebSocket不存在！");
        }
        try {
            if (null == userIdList || userIdList.isEmpty()) {
                webSocket.push(message);
            } else {
                webSocket.push(userIdList, message);
            }
            return true;
        } catch (Exception e) {
            throw new RobotException("推送消息到["+topic+"]时失败："+ e.getMessage(), e);
        }
    }

}
