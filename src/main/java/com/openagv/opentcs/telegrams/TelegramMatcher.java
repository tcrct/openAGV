package com.openagv.opentcs.telegrams;

import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import com.openagv.core.interfaces.IResponse;
import com.openagv.core.interfaces.ITelegramSender;
import com.openagv.tools.ToolsKit;

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

    private static final Log logger = LogFactory.get();

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

        if (emptyQueueBeforeEnqueue) {
            checkForSendingNextRequest();
        }
    }

    /**检查是否发送下一个请求*/
    public void checkForSendingNextRequest() {
        logger.debug("检查是否发送下一个请求.");
        if (peekCurrentRequest().isPresent()) {
            telegramSender.sendTelegram(peekCurrentRequest().get());
        }
        else {
            logger.info("没有请求消息发送");
        }
    }

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
                responseTelegram.getTargetPointName().equals(currentRequestTelegram.getTargetPointName())){
            // 在队列中移除第一位的
            requests.remove();
            return true;
        }

        if(ToolsKit.isNotEmpty(currentRequestTelegram)) {
            logger.warn("请求队列没有{}的请求对象，传参的请求对象{}， 队列与最新对应的请求对象不匹配", currentRequestTelegram.getTargetPointName(),responseTelegram.getTargetPointName());
        } else {
            logger.info("接收到请求ID{}的响应，但没有请求正在等响应",responseTelegram.getTargetPointName());
        }

        return false;

    }

}