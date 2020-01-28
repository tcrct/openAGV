package com.robot.mvc.core.interfaces;

/**
 * Created by laotang on 2020/1/22.
 */
public interface ITelegram extends java.io.Serializable {

    /**
     * 取原始协议字符串
     * @return
     */
    String getRawContent();

    /**
     * 设置原始协议字符串
     * @param raw 原始协议字符串
     * @return
     */
    void setRawContent(String raw);

}
