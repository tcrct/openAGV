package com.robot.contrib.netty.udp;


import com.robot.contrib.netty.ConnectionEventListener;
import com.robot.contrib.netty.comm.ClientChannelManager;
import com.robot.contrib.netty.comm.VehicleTelegramDecoder;
import com.robot.contrib.netty.comm.VehicleTelegramEncoder;
import com.robot.mvc.core.interfaces.IRequest;
import com.robot.mvc.core.interfaces.IResponse;
import io.netty.channel.ChannelHandler;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.List;

/**
 * UPD Client Manager
 */
public class UdpClientManager extends ClientChannelManager<IRequest, IResponse> {

    private UdpClientChannelManager channelManager;

    public UdpClientManager(ConnectionEventListener connectionEventListener) {
        channelManager = new UdpClientChannelManager(connectionEventListener,
                this::getChannelHandlers,
                10000,
                true);
    }

    /**
     * 解码及编码器
     */
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
            channelManager.connect(host, port);
        } catch (Exception e) {
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
    public void send(IResponse telegram) {
        channelManager.send(telegram.getRawContent());
    }
}
