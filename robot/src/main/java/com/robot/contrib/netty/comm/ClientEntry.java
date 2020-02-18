package com.robot.contrib.netty.comm;

import com.robot.contrib.netty.ConnectionEventListener;
import io.netty.channel.Channel;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.net.InetSocketAddress;

import static java.util.Objects.requireNonNull;

/**
 * 客户端对象
 *
 * @author Laotang
 * @date 2020-02-17
 * @since 1.0
 */
public class ClientEntry {

    /**
     * 客户端host
     */
    private String host;

    /**
     * 客户端port
     */
    private Integer port;

    /**
     * clientEntry对象的关键字
     */
    private String key;

    /**
     * 要向其发送有关连接的事件的处理程序
     */
    private ConnectionEventListener connectionEventListener;

    /**
     * 管理当前通讯的通道, netty channel
     */
    private Channel channel;

    /**
     * 构造方法
     *
     * @param host                    客户端host
     * @param port                    客户端port
     * @param connectionEventListener 处理发送链接事件的监听器
     */
    public ClientEntry(@Nonnull String key, @Nonnull String host,
                       @Nonnull Integer port,
                       @Nonnull ConnectionEventListener connectionEventListener) {
        this.key = requireNonNull(key, "key");
        this.host = requireNonNull(host, "host");
        this.port = requireNonNull(port, "host");
        this.connectionEventListener = requireNonNull(connectionEventListener);
    }

    @Nonnull
    public String getKey() {
        return key;
    }

    @Nonnull
    public ConnectionEventListener getConnectionEventListener() {
        return connectionEventListener;
    }

    @Nullable
    public Channel getChannel() {
        return channel;
    }

    public void setChannel(@Nullable Channel channel) {
        this.channel = channel;
    }

    public boolean isConnected() {
        return channel != null && channel.isActive();
    }

    public void disconnect() {
        if (!isConnected()) {
            return;
        }
        channel.disconnect();
        channel = null;
    }

    @Nullable
    public String getHost() {
        return host;
    }

    @Nullable
    public Integer getPort() {
        return port;
    }

    public static String createClientEntryKey(String host, int port) {
        return host + ":" + port;
    }
}
