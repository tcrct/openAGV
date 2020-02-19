package com.robot.utils;

import com.robot.contrib.netty.ConnectionEventListener;
import com.robot.contrib.netty.comm.*;
import com.robot.contrib.netty.rxtx.RxtxServerChannelManager;
import com.robot.contrib.netty.tcp.TcpServerChannelManager;
import com.robot.contrib.netty.udp.UdpServerChannelManager;
import io.netty.channel.ChannelHandler;

import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Netty网络处理工具
 *
 * @author Laotang
 * @date 2020/2/19.
 * @since 1.0
 */
public class NettyContribKit {

    private static NettyContribKit CONTRIB_KIT;
    private static final Lock lock = new ReentrantLock();
    private IServiceChannelManager serviceChannelManager;
    private static final Map<String, ClientEntry> CLIENT_ENTRIES = new HashMap<>();
    private static int READ_TIMEOUT = 5000;
    private static boolean LOGGING_INITIALLY = true;
    private static final String HOST = "0.0.0.0";
    private static int PORT = 7070;

    private NettyContribKit(String host, int port) {
        getChannelManager(host, port);
    }

    public static NettyContribKit duang() {
        return duang(HOST, PORT);
    }

    /**
     * 自定义host与pont
     *
     * @param host
     * @param port
     * @return
     */
    public static NettyContribKit duang(String host, int port) {
        synchronized (lock) {
            CONTRIB_KIT = new NettyContribKit(host, port);
        }
        return CONTRIB_KIT;
    }

    /**
     * 设置网络通讯管理器
     *
     * @param host 地址
     * @param port 端口
     * @return
     */
    private void getChannelManager(String host, int port) {
        NetChannelType channelType = RobotUtil.getNetChannelType();
        if (NetChannelType.TCP.equals(channelType)) {
            serviceChannelManager = new TcpServerChannelManager(host, port,
                    CLIENT_ENTRIES,
                    this::getChannelHandlers,
                    READ_TIMEOUT,
                    LOGGING_INITIALLY);
        } else if (NetChannelType.UDP.equals(channelType)) {
            serviceChannelManager = new UdpServerChannelManager(host, port,
                    CLIENT_ENTRIES,
                    this::getChannelHandlers,
                    READ_TIMEOUT,
                    LOGGING_INITIALLY);
        } else if (NetChannelType.RXTX.equals(channelType)) {
            serviceChannelManager = new RxtxServerChannelManager(host, port,
                    CLIENT_ENTRIES,
                    this::getChannelHandlers,
                    READ_TIMEOUT,
                    LOGGING_INITIALLY);
        }

        // 初始化服务
        if (null != serviceChannelManager && !serviceChannelManager.isInitialized()) {
            serviceChannelManager.initialize();
        }
    }

    /**
     * 解码及编码器
     */
    private List<ChannelHandler> getChannelHandlers() {
        return Arrays.asList(
                new VehicleTelegramDecoder(),
                new VehicleTelegramEncoder());
    }


    /**
     * 是否初始化
     *
     * @return true为已经初始化
     */
    public boolean isInitialized() {
        return serviceChannelManager.isInitialized();
    }

    /**
     * 终止
     */
    public void terminate() {
        serviceChannelManager.terminate();
    }


    /**
     * 将客户端注册到服务器端
     *
     * @param key      客户端关键字，须保证唯一性
     * @param host     客户端地址
     * @param port     客户端端口
     * @param listener 连接监听器，主要是用于回调netty服务器状态，触发处理事件
     */
    public void register(String key, String host, int port, ConnectionEventListener listener) {
        if (ToolsKit.isEmpty(key)) {
            throw new NullPointerException("注册时，关键字不能为空");
        }
        if (ToolsKit.isEmpty(host)) {
            throw new NullPointerException("注册时，host地址不能为空");
        }
        if (port >= 1000 && port <= 10000) {
            throw new IllegalArgumentException("注册时，客户端port只能在[1000~10000]之间");
        }
        if (ToolsKit.isEmpty(listener)) {
            throw new NullPointerException("注册时，ConnectionEventListener不能为空");
        }
        serviceChannelManager.register(new ClientEntry(key, host, port, listener));
    }


    /**
     * 取消客户端注册
     */
    public void unregister(String key) {
        if (ToolsKit.isEmpty(key)) {
            throw new NullPointerException("取消客户端注册时，关键字不能为空");
        }
        serviceChannelManager.unregister(key);
    }

    /**
     * 重复注册
     *
     * @param key
     */
    public void reregister(String key) {
        if (ToolsKit.isEmpty(key)) {
            throw new NullPointerException("重复注册客户端时，关键字不能为空");
        }
        serviceChannelManager.reregister(key);
    }

    /**
     * 关闭链接
     *
     * @param key
     */
    public void closeConnection(String key) {
        if (ToolsKit.isEmpty(key)) {
            throw new NullPointerException("关闭客户端时，关键字不能为空");
        }
        serviceChannelManager.closeConnection(key);
    }

    /**
     * 是否连接
     *
     * @return 返回true为已经连接
     */
    public boolean isConnected(String key) {
        if (ToolsKit.isEmpty(key)) {
            throw new NullPointerException("检查客户端是否连接时，关键字不能为空");
        }
        return serviceChannelManager.isConnected(key);
    }

    /**
     * 日志开启
     *
     * @param enable true为开启
     */
    public void setLoggingEnabled(String key, boolean enable) {
        if (ToolsKit.isEmpty(key)) {
            throw new NullPointerException("开启客户端日志时，关键字不能为空");
        }
        serviceChannelManager.setLoggingEnabled(key, enable);
    }

    /**
     * 发送报文
     */
    public void send(String key, String message) throws Exception {
        if (ToolsKit.isEmpty(key)) {
            throw new NullPointerException("发送消息时，关键字不能为空");
        }
        if (ToolsKit.isEmpty(message)) {
            throw new NullPointerException("发送消息时，消息内容不能为空");
        }
        serviceChannelManager.send(key, message);
    }

}
