package com.openagv.contrib.netty.rxtx;

import com.openagv.contrib.netty.comm.IChannelManager;
import com.openagv.contrib.netty.comm.VehicleTelegramDecoder;
import com.openagv.contrib.netty.comm.VehicleTelegramEncoder;
import com.openagv.mvc.core.interfaces.IRequest;
import com.openagv.mvc.core.interfaces.IResponse;
import com.openagv.mvc.core.telegram.ITelegramSender;
import io.netty.channel.ChannelHandler;
import org.opentcs.contrib.tcp.netty.ConnectionEventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.List;

/**
 * Created by laotang on 2020/1/20.
 */
public class RxtxServerManager  implements IChannelManager<IRequest, IResponse> {

    private static final Logger LOG = LoggerFactory.getLogger(RxtxServerManager.class);

    private RxtxServerChannelManager channelManager;

    @Override
    public void initialize() {
        channelManager = RxtxServerChannelManager(this::getChannelHandlers,
                commAdapter.getProcessModel().getVehicleIdleTimeout(),
                commAdapter.getProcessModel().isLoggingEnabled());
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
    public void connect(String serialport, int baudrate) {
        try {
            channelManager.connect(serialport, baudrate);
        } catch (Exception e) {
            LOG.warn("串口链接时出现异常: {}, {}", e.getMessage(), e);
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
    public void scheduleConnect(@Nonnull String serialport, int baudrate, long delay) {
        channelManager.scheduleConnect(serialport, baudrate, delay);
    }

    private List<ChannelHandler> getChannelHandlers() {
        ConnectionEventListener<IResponse> eventListener = (ConnectionEventListener<IResponse>) adapter;
        ITelegramSender telegramSender = (ITelegramSender) adapter;

        return Arrays.asList(
                new VehicleTelegramDecoder(eventListener, telegramSender),
                new VehicleTelegramEncoder());
    }

    @Override
    public void send(IResponse telegram) {

    }
}
