package com.openagv.contrib.netty.tcp;

import com.openagv.adapter.AgvCommAdapter;
import com.openagv.contrib.netty.comm.IChannelManager;
import com.openagv.mvc.core.exceptions.AgvException;

import javax.annotation.Nonnull;

/**
 * Created by laotang on 2020/1/25.
 */
public class TcpClientManager implements IChannelManager {

    private static AgvCommAdapter commAdapter;

    public TcpClientManager(AgvCommAdapter adapter) {
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
    public void connect(String host, int port) throws AgvException {

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
