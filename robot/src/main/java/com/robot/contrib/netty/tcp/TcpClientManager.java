package com.robot.contrib.netty.tcp;

import com.robot.adapter.RobotCommAdapter;
import com.robot.contrib.netty.comm.IChannelManager;
import com.robot.mvc.core.exceptions.RobotException;

import javax.annotation.Nonnull;

/**
 * Created by laotang on 2020/1/25.
 */
public class TcpClientManager implements IChannelManager {

    private static RobotCommAdapter commAdapter;

    public TcpClientManager(RobotCommAdapter adapter) {
        this.commAdapter = adapter;
    }

    @Override
    public void initialize() {

    }

    @Override
    public boolean isInitialized() {
        return false;
    }

    @Override
    public void terminate() {

    }

    @Override
    public void connect(String host, int port) throws RobotException {

    }

    @Override
    public void disconnect() {

    }

    @Override
    public boolean isConnected() {
        return false;
    }

    @Override
    public void setLoggingEnabled(boolean enable) {

    }

    @Override
    public void scheduleConnect(@Nonnull String host, int port, long delay) {

    }

    @Override
    public void send(Object telegram) {

    }
}
