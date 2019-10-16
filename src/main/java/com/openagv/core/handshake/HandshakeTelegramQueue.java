package com.openagv.core.handshake;

import com.openagv.core.interfaces.ICallback;
import com.openagv.core.interfaces.IResponse;
import com.openagv.exceptions.AgvException;
import com.openagv.tools.ToolsKit;
import org.apache.log4j.Logger;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;

import static java.util.Objects.requireNonNull;

/***握手电报队列，key为车辆，Value为所有需要响应的报文对象
 * 用于消息握手机制，必须进行消息握手确认处理，如握手不成功，则阻塞该车辆的消息发送，直至握手确认成功
 * 确认成功后，会移除顶部位置的报文对象
 *
 * @author Laotang
 */
public class HandshakeTelegramQueue {

    private static final Logger logger = Logger.getLogger(HandshakeTelegramQueue.class);

    private final static Map<String, LinkedBlockingQueue<HandshakeTelegramDto>> HANDSHAKE_TELEGRAM_QUEUE = new java.util.concurrent.ConcurrentHashMap<>();

    /**
     * 添加到队列
     * @param telegramDto 队列对象
     */
    public void add(HandshakeTelegramDto telegramDto) {
        if(ToolsKit.isEmpty(telegramDto)) {
            throw new NullPointerException("队列对象不能为空");
        }
        IResponse response = requireNonNull(telegramDto.getResponse(),"返回的对象不能为空");
        String deviceId = requireNonNull(response.getDeviceId(),"设备ID不能为空");
        LinkedBlockingQueue<HandshakeTelegramDto> queue =  HANDSHAKE_TELEGRAM_QUEUE.get(deviceId);
        if(ToolsKit.isEmpty(queue)) {
            queue = new LinkedBlockingQueue<>();
        }
        // 添加到队列
        queue.add(telegramDto);
        HANDSHAKE_TELEGRAM_QUEUE.put(deviceId, queue);
    }
    /**
     * 移除元素
     * @param deviceId 设备ID
     * @param key 标识字段，用于确认握手关系
     */
    public void remove(String deviceId, String key) {
        requireNonNull(deviceId, "设备ID不能为空");
        requireNonNull(key, "标识字段不能为空，一般是指验证码之类的唯一标识字段");
        LinkedBlockingQueue<HandshakeTelegramDto> queue =  requireNonNull(HANDSHAKE_TELEGRAM_QUEUE.get(deviceId), "handshake queue is null");
        HandshakeTelegramDto toBeDeleteDto = requireNonNull(queue.peek(), "handshake telegram dto is null");
        IResponse response = requireNonNull(toBeDeleteDto.getResponse(),"");
        String handshakeKey = response.getHandshakeKey();
        if(ToolsKit.isNotEmpty(queue) && ToolsKit.isNotEmpty(handshakeKey) && handshakeKey.equals(key)) {
            //先复制 ??
            HandshakeTelegramDto telegramDto = new HandshakeTelegramDto(toBeDeleteDto);
            // 再移除第一位元素对象
            queue.remove();
            logger.info("remove vehicle["+deviceId+"] telegramDto["+telegramDto.toString()+"] is success!");
            // 指令队列中移除后再发送下一个指令
            ICallback callback =telegramDto.getCallback();
            String requestId = ToolsKit.isEmpty(telegramDto.getRequest()) ? telegramDto.getResponse().getRequestId() : telegramDto.getRequest().getRequestId();
            if(ToolsKit.isNotEmpty(callback) && ToolsKit.isNotEmpty(requestId)) {
                // 回调机制，告诉系统这条指令可以完结了。
                try {
                    callback.call(deviceId, requestId);
                } catch (Exception e) {
                    throw new AgvException(e.getMessage(), e);
                }
            }
        }
        if(ToolsKit.isEmpty(handshakeKey)) {
            throw new AgvException("握手key不能为空");
        }
        if(!handshakeKey.equals(key)) {
            throw new AgvException("提交上来的报文handshakeKey["+key+"]与系统队列中handshakeKey["+handshakeKey+"]不一致！");
        }
    }

    public Iterator<Map.Entry<String, LinkedBlockingQueue<HandshakeTelegramDto>>> getIterator() {
        if(ToolsKit.isEmpty(HANDSHAKE_TELEGRAM_QUEUE)) {
            return null;
        }
        return HANDSHAKE_TELEGRAM_QUEUE.entrySet().iterator();
    }

    public void clearQueue() {
        if(ToolsKit.isNotEmpty(HANDSHAKE_TELEGRAM_QUEUE)) {
            HANDSHAKE_TELEGRAM_QUEUE.clear();
        }
    }
}
