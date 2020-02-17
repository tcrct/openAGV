package com.robot.contrib.netty.tcp;

import com.robot.contrib.netty.ConnectionEventListener;
import com.robot.contrib.netty.comm.ClientEntry;
import com.robot.contrib.netty.comm.ServerChannelManager;
import com.robot.contrib.netty.comm.VehicleTelegramDecoder;
import com.robot.contrib.netty.comm.VehicleTelegramEncoder;
import io.netty.channel.ChannelHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by laotang on 2020/1/25.
 */
public class TcpServerManager extends ServerChannelManager {

    private static final Logger LOG = LoggerFactory.getLogger(TcpServerManager.class);

    private static TcpServerChannelManager tcpServerChannelManager;
    private static TcpServerManager tcpServerManager;
    private static final Map<Object, ClientEntry<Object>> CLIENT_ENTRIES = new HashMap<>();
    private static Lock lock = new ReentrantLock();
    private static int PORT = 9090;
    private static int READ_TIMEOUT = 5000;
    private static boolean LOGGING_INITIALLY = true;


    public static TcpServerManager duang() {
        synchronized (lock) {
            if (null == tcpServerManager) {
                tcpServerManager = new TcpServerManager();
            }
            return tcpServerManager;
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

    private TcpServerManager() {
        tcpServerChannelManager = new TcpServerChannelManager(PORT, CLIENT_ENTRIES,
                this::getChannelHandlers,
                READ_TIMEOUT,
                LOGGING_INITIALLY);
    }

    @Override
    public void initialize() {
        if (!isInitialized()) {
            tcpServerChannelManager.initialize();
        }
    }

    @Override
    public boolean isInitialized() {
        return tcpServerChannelManager.isInitialized();
    }

    @Override
    public void terminate() {
        tcpServerChannelManager.terminate();
    }

    @Override
    public void register(String host, int port, ConnectionEventListener connectionEventListener) {
        try {
            if (!isConnected()) {
                tcpServerChannelManager.register(host, port, connectionEventListener);
            }
        } catch (Exception e) {
            e.printStackTrace();
            LOG.error("注册[{}:{}]时发生异常: {}", host, port, e.getMessage());
        }
    }

    @Override
    public void disconnect(String key) {
        tcpServerChannelManager.closeClientConnection(key);
    }

    @Override
    public boolean isConnected(String key) {
        return tcpServerChannelManager.isClientConnected(key);
    }

    @Override
    public void setLoggingEnabled(String key, boolean enable) {
        tcpServerChannelManager.setLoggingEnabled(key, enable);
    }

    @Override
    public void send(String key, Object telegram) {
        tcpServerChannelManager.send(key, telegram);
    }

}
