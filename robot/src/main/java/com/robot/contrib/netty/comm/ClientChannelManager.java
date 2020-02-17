package com.robot.contrib.netty.comm;

import com.robot.contrib.netty.ConnectionEventListener;
import com.robot.mvc.core.exceptions.RobotException;

/**
 * 网络渠道管理器接口
 *
 * @param <I>     请求对象
 * @param <O>返回对象
 * @author Laotang
 */
public abstract class ClientChannelManager<I, O> implements IChannelManager<I, O> {


    @Override
    public void register(String host, int port, ConnectionEventListener connectionEventListener) {
        throw new RobotException("客户端不需要实现该方法");
    }

    @Override
    public boolean isConnected(String key) {
        throw new RobotException("客户端不需要实现该方法");
    }

    @Override
    public void disconnect(String key) {
        throw new RobotException("客户端不需要实现该方法");
    }

    @Override
    public void setLoggingEnabled(String key, boolean enable) {
        throw new RobotException("客户端不需要实现该方法");
    }

    @Override
    public void send(String key, O telegram) {
        throw new RobotException("客户端不需要实现该方法");
    }

}
