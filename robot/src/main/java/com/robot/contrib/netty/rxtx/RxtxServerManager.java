package com.robot.contrib.netty.rxtx;

import com.robot.adapter.RobotCommAdapter;
import com.robot.contrib.netty.comm.IChannelManager;
import com.robot.contrib.netty.comm.VehicleTelegramDecoder;
import com.robot.contrib.netty.comm.VehicleTelegramEncoder;
import com.robot.mvc.core.exceptions.AgvException;
import com.robot.mvc.core.interfaces.IRequest;
import com.robot.mvc.core.interfaces.IResponse;
import com.robot.mvc.utils.ToolsKit;
import io.netty.channel.ChannelHandler;
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

    private RobotCommAdapter commAdapter;
    private RxtxServerChannelManager channelManager;

    public RxtxServerManager(RobotCommAdapter adapter) {
        this.commAdapter = adapter;
    }

    @Override
    public void initialize() {
        channelManager = new RxtxServerChannelManager(this::getChannelHandlers,
                10000,
                true);
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
            throw new AgvException("串口链接时出现异常: "+e.getMessage(), e);
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
        return Arrays.asList(
                new VehicleTelegramDecoder(),
                new VehicleTelegramEncoder());
    }

    @Override
    public void send(IResponse telegram) {
        if (ToolsKit.isEmpty(telegram) || ToolsKit.isEmpty(telegram.getRawContent())) {
            throw new AgvException("要发送的对象或内容不能为空");
        }
        channelManager.send(telegram);
    }
}
