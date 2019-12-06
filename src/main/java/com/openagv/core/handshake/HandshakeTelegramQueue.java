package com.openagv.core.handshake;

import com.openagv.core.AppContext;
import com.openagv.core.interfaces.ICallback;
import com.openagv.core.interfaces.IRequest;
import com.openagv.core.interfaces.IResponse;
import com.openagv.exceptions.AgvException;
import com.openagv.tools.ToolsKit;
import org.apache.log4j.Logger;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Consumer;

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
     *
     * @param telegramDto 队列对象
     */
    public void add(HandshakeTelegramDto telegramDto) {
        if (ToolsKit.isEmpty(telegramDto)) {
            throw new NullPointerException("队列对象不能为空");
        }
        IResponse response = requireNonNull(telegramDto.getResponse(), "返回的对象不能为空");
        String deviceId = requireNonNull(response.getDeviceId(), "设备ID不能为空");
        LinkedBlockingQueue<HandshakeTelegramDto> queue = HANDSHAKE_TELEGRAM_QUEUE.get(deviceId);
        if (ToolsKit.isEmpty(queue)) {
            queue = new LinkedBlockingQueue<>();
        }
        // 添加到队列
        queue.add(telegramDto);
        HANDSHAKE_TELEGRAM_QUEUE.put(deviceId, queue);
    }

    /**
     * 根据指令名称与参数规则移除元素
     * 一般是用于rptmt，提交传感器开关时用
     *
     * @param deviceId 设备ID
     * @param cmdKey   指令名称
     * @param params   指令参数
     */
    public boolean removeByCmd(String deviceId, String cmdKey, String[] params) {
        requireNonNull(deviceId, "设备ID不能为空");
        requireNonNull(cmdKey, "标识字段不能为空，一般是指验证码之类的唯一标识字段");
        LinkedBlockingQueue<HandshakeTelegramDto> queue = requireNonNull(HANDSHAKE_TELEGRAM_QUEUE.get(deviceId), "handshake queue is null");
        HandshakeTelegramDto toBeDeleteDto = requireNonNull(queue.peek(), "handshake telegram dto is null, cmdKey: " + cmdKey);
        IRequest request = requireNonNull(toBeDeleteDto.getRequest(), "request is null");
        Sensor sensor = requireNonNull((Sensor) request.getPropertiesMap().get(IRequest.SENSOR_FIELD), "Sensor is null");
        // 根据报文对比参数是否允许删除
        if (sensor.isWith(params)) {
            callBackAndRemove(deviceId, queue, toBeDeleteDto);
            return false;
        }
        logger.info("该指令不是监听提交或对比的索引值不一致，请注意第一位元素数据的索引从0开始即：Array[0]为第一位");
        return true;
    }

    private void callBackAndRemove(String deviceId, LinkedBlockingQueue<HandshakeTelegramDto> queue, HandshakeTelegramDto toBeDeleteDto) {
        //先复制 ??
        HandshakeTelegramDto telegramDto = new HandshakeTelegramDto(toBeDeleteDto);
        // 再移除第一位元素对象
        queue.remove();
        logger.info("remove vehicle[" + deviceId + "] telegramDto[" + telegramDto.toString() + "] is success!");
        // 指令队列中移除后再发送下一个指令
        ICallback callback = telegramDto.getCallback();
        String requestId = ToolsKit.isEmpty(telegramDto.getRequest()) ? telegramDto.getResponse().getRequestId() : telegramDto.getRequest().getRequestId();
        // 只有在actionKey不为空的情况下才进行回调处理
        String actionKey = telegramDto.getActionKey();
        if (ToolsKit.isNotEmpty(callback) &&
                ToolsKit.isNotEmpty(requestId) &&
                ToolsKit.isNotEmpty(actionKey)) {
            // 回调机制，告诉系统这条指令可以完结了。
            try {
                callback.call(actionKey, requestId);
            } catch (Exception e) {
                throw new AgvException(e.getMessage(), e);
            }
        }
    }

    /**
     * 移除元素
     *
     * @param deviceId 设备ID
     * @param key      标识字段，用于确认握手关系
     */
    public void remove(String deviceId, String key) {
        requireNonNull(deviceId, "设备ID不能为空");
        requireNonNull(key, "标识字段不能为空，一般是指验证码之类的唯一标识字段");
        LinkedBlockingQueue<HandshakeTelegramDto> queue = requireNonNull(HANDSHAKE_TELEGRAM_QUEUE.get(deviceId), "handshake queue is null");
        HandshakeTelegramDto toBeDeleteDto = requireNonNull(queue.peek(), "handshake telegram dto is null");
        IResponse response = requireNonNull(toBeDeleteDto.getResponse(), "response in null");
        String handshakeKey = response.getHandshakeKey();
        if (ToolsKit.isNotEmpty(queue) &&
                ToolsKit.isNotEmpty(handshakeKey) &&
                handshakeKey.equals(key)) {
            callBackAndRemove(deviceId, queue, toBeDeleteDto);
        }
        if (ToolsKit.isEmpty(handshakeKey)) {
            throw new AgvException("握手key不能为空");
        }
        if (!handshakeKey.equals(key)) {
            throw new AgvException("提交上来的报文handshakeKey[" + key + "]与系统队列中handshakeKey[" + handshakeKey + "]不一致！");
        }
    }

    public Iterator<Map.Entry<String, LinkedBlockingQueue<HandshakeTelegramDto>>> getIterator() {
        if (ToolsKit.isEmpty(HANDSHAKE_TELEGRAM_QUEUE)) {
            return null;
        }
        return HANDSHAKE_TELEGRAM_QUEUE.entrySet().iterator();
    }

    public void clearQueue() {
        if (ToolsKit.isNotEmpty(HANDSHAKE_TELEGRAM_QUEUE)) {
            HANDSHAKE_TELEGRAM_QUEUE.clear();
        }
    }

    public LinkedBlockingQueue<HandshakeTelegramDto> get(String key) {
        if (ToolsKit.isEmpty(HANDSHAKE_TELEGRAM_QUEUE)) {
            return null;
        }
        return HANDSHAKE_TELEGRAM_QUEUE.get(key);
    }

    /**
     * 验证是否存在队列中
     *
     * @param deviceId
     * @param key
     * @return
     */
    public boolean containsCmdKey(String deviceId, String key) {
        LinkedBlockingQueue<HandshakeTelegramDto> queue = HANDSHAKE_TELEGRAM_QUEUE.get(deviceId);
        if (null != queue && !queue.isEmpty()) {
            HandshakeTelegramDto dto = queue.peek();
            return key.equals(dto.getResponse().getCmdKey());
        }
        return false;
    }

    /**
     * 验证是否存在队列中
     *
     * @param deviceId
     * @param key
     * @return 不存在返回false
     */
    public boolean containsHandshakeKey(String deviceId, String key) {
        LinkedBlockingQueue<HandshakeTelegramDto> queue = HANDSHAKE_TELEGRAM_QUEUE.get(deviceId);
        if (null != queue && !queue.isEmpty()) {
            HandshakeTelegramDto dto = queue.peek();
            if (ToolsKit.isEmpty(dto)) {
                return false;
            }
            return key.equals(dto.getResponse().getHandshakeKey());
        }

        return false;
    }

    /**
     * 场景：
     * 发送第1位的命令到车辆/设备后，由于网络原因，没有收到回复，所以队列中的元素没有发生变化，但车辆收到请求后，执行动作后，将第2位的回复成功发送到服务器
     * 此时，因为没有收到第一位命令的回复，由于定时机制，服务器再次发送第1位的命令，此时，正确返回响应，服务器将队列中的第1位移除，第2位上升至第1位进行监听回复
     * 但，由于第2位的回复在之前已经成功执行完成了，所以不会再次重发，故此服务器一直在等待回复，导致程序执行不下去
     *
     * 解决方案：
     * 将超前上报的请求缓存起来，以crc验证码作为key，直到程序执行该监听回复时，重复监听3次后，还是没有回复，则可以先查缓存内是否有与该回复对应的缓存记录
     * 如果有，则清空后再执行后续的请求，如果没有，则继续等待
     *
     * @param requestType 请求类型
     * @param telegram  请求内容
     * @param cmdKey 命令指令
     * @param deviceId 设备/车辆ID
     * @param key  CRC验证码关键字
     */
    public void cacheAdvanceReportToMap(String requestType, String telegram, String cmdKey, String deviceId, String key) {
        IRequest advanceRequest = AppContext.getActionRequests().get(key);
        // 如果是MakerwitActionsAlgorithm发出的请求，则requestType有内容，不为空的。
        boolean isAgvRequest = ToolsKit.isNotEmpty(requestType) &&
                ("baseresponse".equalsIgnoreCase(requestType) || "baserequest".equalsIgnoreCase(requestType));
        System.out.println(isAgvRequest + "######cacheAdvanceReportToMap######: " +requestType);
        //如果不是MakerwitActionsAlgorithm发出的请求，则添加到缓存中
        if (!isAgvRequest && ToolsKit.isNotEmpty(advanceRequest) && cmdKey.equals(advanceRequest.getCmdKey())) {
            logger.info("设备[" + deviceId + "]上报的请求[" + telegram + "]不是在握手队列中的第一位，属于提前上报，先暂存放在AdvanceReportMap集合");
            AppContext.getAdvanceReportMap().put(key, advanceRequest);
        }
    }

    /**
     * 不使用的代码，先保留
     *
     * 如果与握手队列中的第一位的不相等，则进行遍历队列，找出对应的handshakeKey进行比较，
     * 如果与提交的key相等的话，则将该handshakeKey对应元素前面的元素删除
     * 如，提交过的来key为2，存在于队列中的第2位，则将1，2这两位的元素移除，以确保程序执行。
     *  | 1 |
     * | 2 |
     * | 3 |
     * 场景：
     * 发送第1位的命令到车辆/设备后，由于网络原因，没有收到回复，所以队列中的元素没有发生变化，但车辆收到请求后，执行动作后，将第2位的回复成功发送到服务器
     * 此时，在没有收到第一位命令的回复，由于定时机制，服务器再次发送第1位的命令，此时，正确返回响应，服务器将队列中的第1位移除，第2位上升至第1位进行监听回复
     * 但，由于第2位的回复在之前已经成功执行完成了，所以不会再次重发，故此服务器一直在等待回复，导致程序执行不下去     *
     *
     */
    public void existHandshakeKeyAndDelete(String deviceId, String key) {
        LinkedBlockingQueue<HandshakeTelegramDto> queue = HANDSHAKE_TELEGRAM_QUEUE.get(deviceId);

        int index = 0;
        for(Iterator<HandshakeTelegramDto> iterator = queue.iterator(); iterator.hasNext();) {
            HandshakeTelegramDto dto = iterator.next();
            if(key.equals(dto.getResponse().getHandshakeKey())) {
                index++;
                break;
            }
        }
        // 不存在
        if(index == 0) {
            logger.info("该元素不存在于队列中,退出！");
            return;
        }
        logger.info("该元素存在于队列中的第 "+index+" 位，需要将前面的元素移除再进行操作！");
        // 移除前面的元素
        for(int i=0; i<index; i++) {
            queue.remove();
            logger.info("移除第 "+i+" 位成功");
        }

        // 删除提交的那个元素
        remove(deviceId, key);
    }
}
