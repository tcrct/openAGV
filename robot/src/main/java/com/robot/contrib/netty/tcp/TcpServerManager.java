package com.robot.contrib.netty.tcp;

import com.robot.adapter.RobotCommAdapter;
import com.robot.contrib.netty.comm.IChannelManager;
import com.robot.contrib.netty.udp.UdpServerManager;
import com.robot.mvc.core.exceptions.RobotException;

import javax.annotation.Nonnull;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by laotang on 2020/1/25.
 */
public class TcpServerManager implements IChannelManager {

    private static RobotCommAdapter commAdapter;
    private static TcpServerManager tcpServerManager;
    private static Lock lock = new ReentrantLock();

    public static TcpServerManager duang(RobotCommAdapter commAdapter) {
        synchronized (lock) {
            if (null == tcpServerManager) {
                tcpServerManager = new TcpServerManager(commAdapter);
            }
            return tcpServerManager;
        }
    }

    private TcpServerManager(RobotCommAdapter adapter) {
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
        if (!isConnected()) {
            tcpServerManager.connect(host, port);
        }
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
