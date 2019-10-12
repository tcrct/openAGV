package com.openagv.opentcs.telegrams;

import com.openagv.core.AppContext;
import com.openagv.core.interfaces.IResponse;
import com.openagv.core.interfaces.ITelegramSender;
import com.openagv.tools.ToolsKit;
import org.apache.log4j.Logger;

import javax.annotation.Nonnull;
import java.util.LinkedList;
import java.util.Optional;
import java.util.Queue;

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

    /** 电报发送接口*/
    private ITelegramSender telegramSender;

    public TelegramMatcher(ITelegramSender telegramSender) {
        this.telegramSender = requireNonNull(telegramSender, "telegramSender");
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

        boolean emptyQueueBeforeEnqueue = requests.isEmpty();
        requests.add(requestTelegram);
        logger.info("添加到队列成功: "+ requestTelegram.toString());

        if (emptyQueueBeforeEnqueue) {
            checkForSendingNextRequest();
        }
    }

    /**检查是否发送下一个请求*/
    public void checkForSendingNextRequest() {
        logger.info("检查是否发送下一个请求.");
        if (peekCurrentRequest().isPresent()) {
            IResponse response = peekCurrentRequest().get();
            if(AppContext.isHandshakeListener()) {
                AppContext.setTelegramQueue(new TelegramQueueDto(response.getDeviceId(),
                        response.getHandshakeKey(),
                        response));
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

        //取出队列中的第一位的请求，该请求视为当前请求,放在队列里的是逻辑处理后返回的IResponse
        IResponse currentRequestTelegram = requests.peek();
        // 判断该回复里的请求到达点与队列里的是否一致，如果一致，则返回true
        if(ToolsKit.isNotEmpty(currentRequestTelegram) &&
                responseTelegram.getNextPointName().equals(currentRequestTelegram.getNextPointName())){
            // 在队列中移除第一位的
            requests.remove();
            logger.info("匹配成功，在队列中移除第一位的元素记录");
            return true;
        }

        if(ToolsKit.isNotEmpty(currentRequestTelegram)) {
            logger.warn("请求队列没有{}的请求对象，传参的请求对象"+currentRequestTelegram.getNextPointName()+"， 队列与最新对应的请求对象不匹配");
        } else {
            logger.info("接收到请求ID["+responseTelegram.getRequestId()+"]的响应，但没有请求正在等响应");
        }

        return false;

    }

}