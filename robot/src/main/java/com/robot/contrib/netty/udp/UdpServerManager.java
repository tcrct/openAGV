//package com.robot.contrib.netty.udp;
//
//import com.robot.contrib.netty.comm.*;
//import com.robot.mvc.core.interfaces.IRequest;
//import com.robot.mvc.core.interfaces.IResponse;
//import io.netty.channel.ChannelHandler;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import java.util.Arrays;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//import java.util.concurrent.locks.Lock;
//import java.util.concurrent.locks.ReentrantLock;
//
///**
// * UPD Server Manager
// */
//public class UdpServerManager extends ServerChannelManager<IRequest, IResponse> {
//
//    private static final Logger LOG = LoggerFactory.getLogger(UdpServerManager.class);
//
//    private UdpServerChannelManager udpServerChannelManager;
//    private static UdpServerManager udpServerManager;
//    private static Lock lock = new ReentrantLock();
//    private static final Map<Object, ClientEntry> CLIENT_ENTRIES = new HashMap<>();
//    private static int READ_TIMEOUT = 5000;
//    private static boolean LOGGING_INITIALLY = true;
//
//    public static UdpServerManager duang() {
//        synchronized (lock) {
//            if (null == udpServerManager) {
//                udpServerManager = new UdpServerManager();
//            }
//            return udpServerManager;
//        }
//    }
//
//    private UdpServerManager() {
//        udpServerChannelManager = new UdpServerChannelManager(CLIENT_ENTRIES,
//                this::getChannelHandlers,
//                READ_TIMEOUT,
//                LOGGING_INITIALLY);
//    }
//
//    /**
//     * 解码及编码器
//     */
//    private List<ChannelHandler> getChannelHandlers() {
//        return Arrays.asList(
//                new VehicleTelegramDecoder(),
//                new VehicleTelegramEncoder());
//    }
//
//    @Override
//    public void initialize() {
//        if (!isInitialized()) {
//            udpServerChannelManager.initialize();
//        }
//    }
//
//    @Override
//    public boolean isInitialized() {
//        return udpServerChannelManager.isInitialized();
//    }
//
//    @Override
//    public void terminate() {
//        udpServerChannelManager.terminate();
//    }
//
//    @Override
//    public void register(ClientEntry clientEntry) {
//        try {
//            if (!isConnected(clientEntry.getKey())) {
//                udpServerChannelManager.register(clientEntry);
//            }
//        } catch (Exception e) {
//            LOG.error("注册[{}]时发生异常: {}", clientEntry.getKey(), e.getMessage());
//            e.printStackTrace();
//        }
//    }
//
//    @Override
//    public void disconnect(String key) {
//        udpServerChannelManager.closeClientConnection(key);
//    }
//
//    @Override
//    public boolean isConnected(String key) {
//        return udpServerChannelManager.isClientConnected(key);
//    }
//
//    @Override
//    public void setLoggingEnabled(String key, boolean enable) {
//        udpServerChannelManager.setLoggingEnabled(key, enable);
//    }
//
//    @Override
//    public void send(String key, IResponse telegram) {
//        udpServerChannelManager.send(key, telegram);
//    }
//}
