package com.robot.contrib.netty.comm;


/**
 * 网络渠道管理器接口
 *
 * @param O 发送泛型对象
 * @author Laotang
 */
public interface IServiceChannelManager {

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
     * 将客户端注册到服务器端
     *
     * @param clientEntry 客户端对象
     */
    void register(ClientEntry clientEntry);

    /**
     * 断开连接
     */
    void unregister(String key);

    /**
     * 重复注册
     *
     * @param key
     */
    void reregister(String key);

    /**
     * 关闭链接
     *
     * @param key
     */
    void closeConnection(String key);

    /**
     * 是否连接
     *
     * @return 返回true为已经连接
     */
    boolean isConnected(String key);

    /**
     * 日志开启*
     *
     * @param enable true为开启
     */
    void setLoggingEnabled(String key, boolean enable);

    /**
     * 发送报文
     */
    void send(String key, String telegram) throws Exception;

}
