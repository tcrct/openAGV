package com.openagv.mvc.net.udp.client;

import com.robot.agv.common.telegrams.Request;
import com.robot.agv.common.telegrams.Response;
import com.robot.agv.common.telegrams.TelegramSender;
import com.robot.agv.vehicle.RobotCommAdapter;
import com.robot.agv.vehicle.net.IChannelManager;
import com.robot.agv.vehicle.net.netty.comm.VehicleTelegramDecoder;
import com.robot.agv.vehicle.net.netty.comm.VehicleTelegramEncoder;
import io.netty.channel.ChannelHandler;
import org.opentcs.contrib.tcp.netty.ConnectionEventListener;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.List;

/**
 * UPD Client Manager
 */
public class UdpClientManager implements IChannelManager<Request, Response> {

    private RobotCommAdapter robotCommAdapter;
    private UdpClientChannelManager channelManager;


    public UdpClientManager(RobotCommAdapter commAdapter) {
        robotCommAdapter = commAdapter;

        channelManager = new UdpClientChannelManager(commAdapter,
                this::getChannelHandlers,
                commAdapter.getProcessModel().getVehicleIdleTimeout(),
                commAdapter.getProcessModel().isLoggingEnabled());
    }

    private List<ChannelHandler> getChannelHandlers() {
        ConnectionEventListener<Response> eventListener = (ConnectionEventListener<Response>)robotCommAdapter;
        TelegramSender telegramSender = (TelegramSender)robotCommAdapter;

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
    public void send(Request telegram) {
        channelManager.send(telegram.getRawContent());
    }
}
