package com.robot.contrib.netty.comm;

import com.robot.contrib.netty.ConnectionEventListener;
import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.logging.LoggingHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;

/**
 * 服务器端基类
 *
 * @author Laotang
 * @since 1.0
 */
public abstract class AbstractServerChannelManager implements IServiceChannelManager {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractServerChannelManager.class);

    // tcp用
    protected ServerBootstrap serverBootstrap;
    // udp or rxtx用
    protected Bootstrap bootstrap;
    protected ChannelFuture serverChannelFuture;
    protected Map<String, ClientEntry> clientEntries;
    protected Supplier<List<ChannelHandler>> channelSupplier;
    protected int readTimeout = 5000;
    protected boolean loggingInitially = true;
    protected String host = "0.0.0.0";
    protected int port = 7070;
    protected boolean initialized;

    /**
     * 构造方法
     *
     * @param host             服务器地址，默认为 0.0.0.0
     * @param port             服务器端口，默认为 7070
     * @param clientEntries    客户端ClientEntry Map集合
     * @param channelSupplier  handler集合
     * @param readTimeout      读超时
     * @param loggingInitially 是否开启日志
     */
    public AbstractServerChannelManager(String host, int port,
                                        Map<String, ClientEntry> clientEntries,
                                        Supplier<List<ChannelHandler>> channelSupplier,
                                        int readTimeout,
                                        boolean loggingInitially) {
        this.host = checkHost(host);
        this.port = checkPort(port);
        this.clientEntries = requireNonNull(clientEntries, "clientEntries");
        this.channelSupplier = requireNonNull(channelSupplier, "channelSupplier");
        this.readTimeout = readTimeout;
        this.loggingInitially = loggingInitially;
    }

    private int checkPort(int port) {
        if (port < 0 || port > 0xFFFF)
            port = this.port;
        return port;
    }

    private String checkHost(String hostname) {
        if (hostname == null) {
            hostname = this.host;
        }
        return hostname;
    }

    /**
     * 是否初始化成功
     *
     * @return
     */
    public boolean isInitialized() {
        return initialized;
    }

    /**
     * 终止Udp通讯服务器
     */
    public void terminate() {
        if (!initialized) {
            throw new IllegalArgumentException("没有初始化");
        }
        serverChannelFuture.channel().close();
        serverChannelFuture = null;
        if (null != bootstrap) {
            bootstrap.config().group().shutdownGracefully();
        }
        if (null != serverBootstrap) {
            serverBootstrap.config().group().shutdownGracefully();
        }
        initialized = false;
    }

    /**
     * 注册客户端
     *
     * @param clientEntry 客户端实体，仅简单包装了host,port,channel待相关信息
     */
    @Override
    public void register(ClientEntry clientEntry) {
        if (!initialized) {
            throw new IllegalArgumentException("服务没有初始化成功");
        }
        String key = clientEntry.getKey();
        if (clientEntries.containsKey(key)) {
            LOG.error("该客户端[{}]已经存在，不能重复注册", key);
            return;
        }

        /**
         *
         */
        String host = clientEntry.getHost();
        if (null != serverBootstrap) {
            ClientEntry subClientEntry = clientEntries.get(host);
            if (null != subClientEntry) {
                Channel channel = subClientEntry.getChannel();
                if (null != channel) {
                    clientEntry.setChannel(channel);
                    LOG.info("Map[clientEntries]里已经存在Host[{}]的Channel, 将id为{}的Channel设置到ClientEntry里", host, channel.id());
                    clientEntries.remove(host);
                    LOG.info("再移除Map[clientEntries]里以{}为主键的对象，以注册key[{}]的为准！", host, key);
                }
            }
        }

        LOG.warn("注册客户端[{}]成功, EndPoint: [{}:{}]", key, clientEntry.getHost(), clientEntry.getPort());
        // 如果不是TCP模式时
        if (null == serverBootstrap) {
            clientEntry.getConnectionEventListener().onConnect();
            clientEntry.setChannel(serverChannelFuture.channel());
        }
        clientEntries.put(key, clientEntry);
    }

    /**
     * 根据关键字key删除客户端
     *
     * @param key 客户端关键字，须保证唯一
     */
    @Override
    public void unregister(String key) {
        if (!initialized) {
            throw new IllegalArgumentException("服务没有初始化成功");
        }

        ClientEntry client = clientEntries.remove(key);
        if (client != null) {
            client.disconnect();
        }
    }

    /**
     * 重复注册
     *
     * @param key 客户端关键字，须保证唯一
     */
    @Override
    public void reregister(String key) {
        ClientEntry clientEntry = clientEntries.get(key);
        if (null == clientEntry) {
            throw new NullPointerException("根据[" + key + "]查找不到对应的ClientEntry对象，可能没有注册成功，请检查！");
        }
        String host = clientEntry.getHost();
        int port = clientEntry.getPort();
        ConnectionEventListener connectionEventListener = clientEntry.getConnectionEventListener();
        unregister(key);
        register(new ClientEntry(key, host, port, connectionEventListener));
    }

    /**
     * 根据关键字，关闭客户端渠道
     *
     * @param key 客户端关键字，须保证唯一
     */
    @Override
    public void closeConnection(String key) {
        if (!initialized) {
            throw new IllegalArgumentException("没有初始化");
        }
        boolean isConnection = isConnected(key);
        if (!isConnection) {
            LOG.warn("该客户端[{}]没有链接成功", key);
            return;
        }
        if (isConnection) {
            try {
                unregister(key);
                clientEntries.get(key).getChannel().disconnect();
                clientEntries.get(key).setChannel(null);
                LOG.warn("关闭客户端[{}]成功", key);
            } catch (Exception e) {
                LOG.warn("关闭客户端[{}]成功时出错: {}", key, e.getMessage(), e);
            }
        }
    }

    /**
     * 设置日志是否开启
     *
     * @param key     客户端关键字，须保证唯一
     * @param enabled 是否开启，true为开启
     */
    public void setLoggingEnabled(String key, boolean enabled, Class<?> channelManagerClass, String loggingName) {
        if (!initialized) {
            throw new IllegalArgumentException("服务没有初始化成功");
        }

        ClientEntry entry = clientEntries.get(key);
        if (null == entry) {
            throw new NullPointerException("根据[" + key + "]查找不到对应的ClientEntry对象，可能没有注册成功，请检查！");
        }

        Channel channel = entry.getChannel();
        if (null == channel) {
            LOG.debug("根据[{}]没有找到对应的channel/pipeline，退出处理！", key);
            return;
        }

        ChannelPipeline pipeline = channel.pipeline();
        if (enabled && pipeline.get(loggingName) == null) {
            pipeline.addFirst(loggingName,
                    new LoggingHandler(channelManagerClass));
        } else if (!enabled && pipeline.get(loggingName) != null) {
            pipeline.remove(loggingName);
        }
    }

    /**
     * 根据关键字判断是否连接成功
     *
     * @param key 客户端关键字，须保证唯一
     * @return
     */
    @Override
    public boolean isConnected(String key) {
        return null !=serverBootstrap ?
                (serverChannelFuture != null
                && clientEntries.containsKey(key)
                && clientEntries.get(key).getChannel() != null
                && clientEntries.get(key).getChannel().isActive()) : (serverChannelFuture != null && clientEntries.containsKey(key));
    }

    /**
     * 发送消息
     *
     * @param key     客户端关键字，须保证唯一
     * @param message 发送的内容
     */
    public synchronized void send(String key, Object message) throws Exception {
        if (!initialized) {
            throw new IllegalArgumentException("服务没有初始化成功");
        }

        if (!isConnected(key)) {
            LOG.warn("发送失败, [{}]没有链接成功。发送内容：{}", key, message);
            return;
        }

        ClientEntry clientEntry = clientEntries.get(key);
        if (null == clientEntry) {
            throw new NullPointerException("根据[" + key + "]查找不到对应的ClientEntry对象，可能没有注册成功，请检查！");
        }
        try {
            clientEntry.getChannel().writeAndFlush(message);
        } catch (Exception e) {
            LOG.error("发送到[{}:{}]失败: {}", clientEntry.getHost(), clientEntry.getPort(), e.getMessage());
            throw new RuntimeException(e.getMessage(), e);
        }
    }
}
