package com.robot.utils;

import com.robot.mvc.core.interfaces.IResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 报文发送工具
 *
 * @author Laotang
 * @since 1.0
 * @date 2020-04-07
 */
public class TelegramSendKit {

    private static final Logger LOG = LoggerFactory.getLogger(TelegramSendKit.class);

    private static TelegramSendKit telegramSendKit;
    private static Lock lock = new ReentrantLock();


    public static TelegramSendKit duang(){
        if (null == telegramSendKit) {
            try {
                lock.lock();
                telegramSendKit = new TelegramSendKit();
            } catch (Exception e) {
                LOG.error("初始化报文发送工具时出错: " + e.getMessage(), e);
            } finally {
                lock.unlock();
            }
        }
        return telegramSendKit;
    }

    private String key;
    private String telegramMsg;

    /**
     * 客户端关键字
     * @param key 客户端关键字，须保证唯一性
     * @return
     */
    public TelegramSendKit key(String key) {
        this.key = key;
        return this;
    }

    /**
     * 需要发送报文字符串
     * @param message 发送的报文内容
     */
    public TelegramSendKit message(String message) {
        this.telegramMsg = message;
        return this;
    }

    /**
     * 需要发送的响应对象
     *
     * @param response 响应对象
     */
    public TelegramSendKit response(IResponse response) {
        this.telegramMsg = response.getRawContent();
        return this;
    }

    /**
     * 发送
     * @throws Exception
     */
    public void send() {
        try {
            ServerContribKit.duang().send(key, telegramMsg);
        } catch (Exception e) {
            LOG.error("发送报文时出错: "+ e.getMessage(), e);
        }
    }
}
