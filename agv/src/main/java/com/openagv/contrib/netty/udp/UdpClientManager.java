package com.openagv.contrib.netty.udp;


import com.openagv.adapter.AgvCommAdapter;
import com.openagv.contrib.netty.comm.VehicleTelegramDecoder;
import com.openagv.contrib.netty.comm.VehicleTelegramEncoder;
import com.openagv.mvc.core.interfaces.IRequest;
import com.openagv.mvc.core.interfaces.IResponse;
import com.openagv.mvc.core.telegram.ITelegramSender;
import io.netty.channel.ChannelHandler;
import org.opentcs.contrib.tcp.netty.ConnectionEventListener;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.List;

/**
 * UPD Client Manager
 */
public class UdpClientManager implements IChannelManager<IRequest, IResponse> {

    private AgvCommAdapter adapter;
    private UdpClientChannelManager channelManager;


    public UdpClientManager(AgvCommAdapter commAdapter) {
        adapter = commAdapter;

        channelManager = new UdpClientChannelManager(commAdapter,
                this::getChannelHandlers,
                commAdapter.getProcessModel().getVehicleIdleTimeout(),
                commAdapter.getProcessModel().isLoggingEnabled());
    }

    private List<ChannelHandler> getChannelHandlers() {
        ConnectionEventListener<IResponse> eventListener = (ConnectionEventListener<IResponse>) adapter;
        ITelegramSender telegramSender = (ITelegramSender) adapter;

        return Arrays.asList(
                new VehicleTelegramDecoder(eventListener, telegramSender),
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
    public void send(IRequest telegram) {
        channelManager.send(telegram.getRawContent());
    }
}
