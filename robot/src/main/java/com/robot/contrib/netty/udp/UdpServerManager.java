package com.robot.contrib.netty.udp;


import com.robot.adapter.RobotCommAdapter;
import com.robot.contrib.netty.comm.IChannelManager;
import com.robot.contrib.netty.comm.VehicleTelegramDecoder;
import com.robot.contrib.netty.comm.VehicleTelegramEncoder;
import com.robot.mvc.core.interfaces.IRequest;
import com.robot.mvc.core.interfaces.IResponse;
import io.netty.channel.ChannelHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * UPD Server Manager
 */
public class UdpServerManager implements IChannelManager<IRequest, IResponse> {

    private static final Logger LOG = LoggerFactory.getLogger(UdpServerManager.class);

    private RobotCommAdapter adapter;
    private UdpServerChannelManager channelManager;
    private static UdpServerManager udpServerManager;
    private static Lock lock = new ReentrantLock();

    public static UdpServerManager duang(RobotCommAdapter commAdapter) {
        synchronized (lock) {
            if (null == udpServerManager) {
                udpServerManager = new UdpServerManager(commAdapter);
            }
            return udpServerManager;
        }
    }

    private UdpServerManager(RobotCommAdapter commAdapter) {
        adapter = commAdapter;

        channelManager = new UdpServerChannelManager(commAdapter,
                this::getChannelHandlers,
                10000,
                true);
    }

    /**解码及编码器*/
    private List<ChannelHandler> getChannelHandlers() {

        return Arrays.asList(
                new VehicleTelegramDecoder(),
                new VehicleTelegramEncoder());
    }

    @Override
    public void initialize() {
        channelManager.initialized();
    }

    @Override
    public boolean isInitialized() {
        return channelManager.isInitialized();
    }

    @Override
    public void terminate() {
        channelManager.terminate();
    }

    @Override
    public void connect(String host, int port) {
        try {
            if (!isConnected()) {
                channelManager.connect(host, port);
            }
        } catch (Exception e) {
            LOG.error("连接[{}:{}]时发生异常: {}", host, port, e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void disconnect() {
        channelManager.disconnect();
    }

    @Override
    public boolean isConnected() {
        return channelManager.isConnected();
    }

    @Override
    public void setLoggingEnabled(boolean enable) {
        channelManager.setLoggingEnabled(enable);
    }

    @Override
    public void scheduleConnect(@Nonnull String host, int port, long delay) {
        channelManager.scheduleConnect(host, port, delay);
    }

    @Override
    public void send(IResponse response) {
        channelManager.send(response.getRawContent());
    }
}
