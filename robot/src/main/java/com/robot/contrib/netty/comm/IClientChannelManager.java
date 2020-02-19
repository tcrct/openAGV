package com.robot.contrib.netty.comm;

import com.robot.mvc.core.exceptions.RobotException;

import javax.annotation.Nonnull;

/**
 * 客户端网络渠道管理器接口
 *
 * @param <O>发送泛型对象
 * @author Laotang
 */
public interface IClientChannelManager<O> {

    /**
     * 初始化
     */
    void initialize();

    /**
     * 是否初始化
     *
     * @return true为已经初始化
     */
    boolean isInitialized();

    /**
     * 终止
     */
    void terminate();

    /**
     * 连接客户端
     *
     * @param host 客户端地址
     * @param port 客户端端口
     */
    void connect(String host, int port) throws RobotException;

    /**
     * 断开连接
     */
    void disconnect();

    /**
     * 是否连接
     *
     * @return 返回true为已经连接
     */
    boolean isConnected();

    /**
     * 日志开启*
     *
     * @param enable true为开启
     */
    void setLoggingEnabled(boolean enable);

    /**
     * 重连
     */
    void scheduleConnect(@Nonnull String host, int port, long delay);

    /**
     * 发送报文
     */
    void send(O telegram);

}
