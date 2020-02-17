package com.robot.contrib.netty.comm;

import com.robot.mvc.core.exceptions.RobotException;

import javax.annotation.Nonnull;

/**
 * 网络渠道管理器接口
 *
 * @param <I>     请求对象
 * @param <O>返回对象
 * @author Laotang
 */
public abstract class ServerChannelManager<I, O> implements IChannelManager<I, O> {


    @Override
    public void connect(String host, int port) throws RobotException {
        throw new RobotException("服务器端不需要实现该方法");
    }

    @Override
    public boolean isConnected() {
        throw new RobotException("服务器端不需要实现该方法");
    }

    @Override
    public void disconnect() {
        throw new RobotException("服务器端不需要实现该方法");
    }

    @Override
    public void setLoggingEnabled(boolean enable) {
        throw new RobotException("服务器端不需要实现该方法");
    }

    @Override
    public void send(O telegram) {
        throw new RobotException("服务器端不需要实现该方法");
    }

    @Override
    public void scheduleConnect(@Nonnull String host, int port, long delay) {
        throw new RobotException("服务器端不需要实现该方法");
    }

}
