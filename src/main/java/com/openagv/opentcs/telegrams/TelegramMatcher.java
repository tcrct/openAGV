package com.openagv.opentcs.telegrams;

import com.openagv.core.AppContext;
import com.openagv.core.handshake.HandshakeTelegramDto;
import com.openagv.core.handshake.HandshakeTelegramQueue;
import com.openagv.core.interfaces.IResponse;
import com.openagv.core.interfaces.ITelegramSender;
import com.openagv.tools.ToolsKit;
import org.apache.log4j.Logger;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

import static java.util.Objects.requireNonNull;

/**
 * 请求响应匹配器
 *
 * @author Laotang
 */
public class TelegramMatcher {

    private static final Logger logger = Logger.getLogger(TelegramMatcher.class);

    /**请求队列*/
    private final Queue<IResponse> requests = new LinkedList<>();
    private final Map<String, Queue<String>> nextPointMap = new ConcurrentHashMap<>();
    /** 电报发送接口*/
    private ITelegramSender telegramSender;
    /**握手对象*/
    private HandshakeTelegramQueue handshakeTelegramQueue;

    public TelegramMatcher(ITelegramSender telegramSender) {
        this.telegramSender = requireNonNull(telegramSender, "telegramSender");
        handshakeTelegramQueue = AppContext.getAgvConfigure().getHandshakeTelegramQueue();
    }

    public ITelegramSender getTelegramSender() {
        return telegramSender;
    }

    /**
     *  将请求电报加入队列中
     * @param requestTelegram   请求电报,经过业务逻辑处理返回的IResponse，其实就是逻辑处理完成后返回的请求报文信息
     */
    public void enqueueRequestTelegram(@Nonnull IResponse requestTelegram) {
        requireNonNull(requestTelegram, "requestTelegram");

        /*
        boolean emptyQueueBeforeEnqueue = requests.isEmpty();
        requests.add(requestTelegram);
        logger.info("添加到队列成功: "+ requestTelegram.toString());
        if (emptyQueueBeforeEnqueue) {
            checkForSendingNextRequest();
        }
        */
        // 将所有经过的点(不包括起始点)放入队列中
        nextPointMap.put(requestTelegram.getDeviceId(), new LinkedBlockingQueue<>(requestTelegram.getNextPointNames()));
        if(AppContext.isHandshakeListener()) {
            handshakeTelegramQueue.add(new HandshakeTelegramDto(requestTelegram));
            logger.info("添加到握手队列["+requestTelegram.getDeviceId()+"]成功: "+ requestTelegram.getHandshakeKey());
        }
        telegramSender.sendTelegram(requestTelegram);
    }

    /**检查是否发送下一个请求*/
    public void checkForSendingNextRequest() {
        logger.info("检查是否发送下一个请求.");
        if (peekCurrentRequest().isPresent()) {
            IResponse response = peekCurrentRequest().get();
            if(AppContext.isHandshakeListener()) {
                handshakeTelegramQueue.add(new HandshakeTelegramDto(response));
                logger.info("添加到握手队列["+response.getDeviceId()+"]成功: "+ response.getHandshakeKey());
            }
            telegramSender.sendTelegram(response);
        }
        else {
            logger.info("没有请求消息发送");
        }
    }

    /**
     * add        增加一个元索                     如果队列已满，则抛出一个IIIegaISlabEepeplian异常
     * remove   移除并返回队列头部的元素    如果队列为空，则抛出一个NoSuchElementException异常
     * element  返回队列头部的元素             如果队列为空，则抛出一个NoSuchElementException异常
     * offer       添加一个元素并返回true       如果队列已满，则返回false
     * poll         移除并返问队列头部的元素    如果队列为空，则返回null
     * peek       返回队列头部的元素             如果队列为空，则返回null
     * put         添加一个元素                      如果队列满，则阻塞
     * take        移除并返回队列头部的元素     如果队列为空，则阻塞
     * @return
     */
    public Optional<IResponse> peekCurrentRequest() {
        return Optional.ofNullable(requests.peek());
    }

    /**
     * 如果与队列中的第一个请求匹配则返回true,并将该请求在队列中移除
     *
     * @param responseTelegram 要匹配的响应电报
     * @return  如果响应与队列中的第一个请求匹配，则返回true
     */
    public boolean tryMatchWithCurrentRequestTelegram(@Nonnull IResponse responseTelegram) {

        java.util.Objects.requireNonNull(responseTelegram, "responseTelegram");

        String cmdKey = responseTelegram.getCmdKey();
        boolean isVehicleArrivalCmd = AppContext.getVehicleArrivalCmdKey().equals(cmdKey);
        if(isVehicleArrivalCmd) {
            return true;
        }
//        //取出队列中的第一位的请求，该请求视为当前请求,放在队列里的是逻辑处理后返回的IResponse
//        IResponse currentRequestTelegram = requests.peek();
//        // 判断该回复里的请求到达点与队列里的是否一致，如果一致，则返回true
//        if (ToolsKit.isNotEmpty(currentRequestTelegram) &&
//                AppContext.getStateRequestCmdKey().equals(cmdKey) &&
//                responseTelegram.getHandshakeKey().equals(currentRequestTelegram.getHandshakeKey())) {
////                responseTelegram.getNextPointNames().equals(currentRequestTelegram.getNextPointNames())) {
//            // 在队列中移除第一位的
//            requests.remove();
//            logger.info("匹配成功，在队列中移除车辆行驶路径记录");
//            return true;
//        }

        return false;
    }

    /**
     * 检查下一个点是否存在队列中
     * @param deviceId  设备ID
     * @param postNextPoint 提交上来的下一个点名称
     * @return  如果存在则返回true
     */
    public boolean checkForVehiclePosition(String deviceId, String postNextPoint) {
        if (ToolsKit.isNotEmpty(deviceId) && ToolsKit.isNotEmpty(postNextPoint)) {
            Queue<String> nextPointQueue = nextPointMap.get(deviceId);
            String nextPoint = nextPointQueue.peek();
            if(ToolsKit.isNotEmpty(postNextPoint) && postNextPoint.equals(nextPoint)) {
                nextPointQueue.remove();
                return true;
            }
        } else {
            logger.warn("车辆移动点不能为空，请确保response.setNextPointNames()设置了所有移动点名称");
        }
        return false;
    }
}