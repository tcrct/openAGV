package com.robot.contrib.netty.rxtx;

import com.robot.contrib.netty.ConnectionEventListener;
import com.robot.contrib.netty.comm.ClientEntry;
import com.robot.contrib.netty.comm.ServerChannelManager;
import com.robot.contrib.netty.comm.VehicleTelegramDecoder;
import com.robot.contrib.netty.comm.VehicleTelegramEncoder;
import com.robot.mvc.core.exceptions.RobotException;
import com.robot.mvc.core.interfaces.IRequest;
import com.robot.mvc.core.interfaces.IResponse;
import com.robot.mvc.utils.ToolsKit;
import io.netty.channel.ChannelHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by laotang on 2020/1/20.
 */
public class RxtxServerManager extends ServerChannelManager<IRequest, IResponse> {

    private static final Logger LOG = LoggerFactory.getLogger(RxtxServerManager.class);

    private RxtxServerChannelManager channelManager;
    private static RxtxServerManager rxtxServerManager;
    private static final Map<Object, ClientEntry<Object>> CLIENT_ENTRIES = new HashMap<>();
    private static Lock lock = new ReentrantLock();

    public static RxtxServerManager duang(String serialport, int baudrate) {
        synchronized (lock) {
            if (null == rxtxServerManager) {
                rxtxServerManager = new RxtxServerManager(serialport, baudrate);
            }
            return rxtxServerManager;
        }
    }

    private RxtxServerManager(String serialport, int baudrate) {
        channelManager = new RxtxServerChannelManager(serialport, baudrate,
                CLIENT_ENTRIES,
                this::getChannelHandlers,
                5000,
                true);
    }

    @Override
    public void initialize() {
        channelManager.initialize();
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
    public void register(String serialport, int baudrate, ConnectionEventListener connectionEventListener) {
        try {
            if (!isConnected()) {
                channelManager.register(serialport, baudrate, connectionEventListener);
            }
        } catch (Exception e) {
            throw new RobotException("串口链接时出现异常: " + e.getMessage(), e);
        }
    }

    @Override
    public void disconnect(String key) {
        channelManager.disconnect(key);
    }

    @Override
    public boolean isConnected(String key) {
        return channelManager.isConnected(key);
    }

    @Override
    public void setLoggingEnabled(String key, boolean enable) {
        channelManager.setLoggingEnabled(key, enable);
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
    public void send(String key, IResponse telegram) {
        if (ToolsKit.isEmpty(telegram) || ToolsKit.isEmpty(telegram.getRawContent())) {
            throw new RobotException("要发送的对象或内容不能为空");
        }
        channelManager.send(telegram);
    }
}
