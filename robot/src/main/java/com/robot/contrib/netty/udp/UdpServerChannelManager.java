package com.robot.contrib.netty.udp;

import com.robot.contrib.netty.ConnectionEventListener;
import com.robot.contrib.netty.comm.ClientEntry;
import com.robot.contrib.netty.comm.ServerConnectionStateNotifier;
import com.robot.mvc.core.exceptions.RobotException;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.CharsetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;

/**
 * Created by laotang on 2019/12/21.
 *
 * @param <I> The type of incoming messages on this UdpServerChannelManager.
 * @param <O> The type of outgoing messages on this UdpServerChannelManager.
 */
public class UdpServerChannelManager<I, O> {

    private static final Logger LOG = LoggerFactory.getLogger(UdpServerChannelManager.class);

    private Bootstrap bootstrap;
    private EventLoopGroup workerGroup;
    private ChannelFuture channelFuture;
    private final Map<String, ClientEntry> clientEntries;
    private Supplier<List<ChannelHandler>> channelSupplier;
    private int readTimeout;
    private final boolean loggingInitially;
    private String host = "0.0.0.0";
    private int port = 7070;

    private boolean initialized;
    private static int BUFFER_SIZE = 64 * 1024;
    private static final String LOGGING_HANDLER_NAME = "ChannelLoggingHandler";

    public UdpServerChannelManager(
            Map<String, ClientEntry> clientEntries,
                                   Supplier<List<ChannelHandler>> channelSupplier,
                                   int readTimeout,
                                   boolean loggingInitially) {
        this.clientEntries = requireNonNull(clientEntries, "clientEntries");
        this.channelSupplier = requireNonNull(channelSupplier, "channelSupplier");
        this.readTimeout = readTimeout;
        this.loggingInitially = loggingInitially;
    }

    public void bind(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void initialize() {
        if (this.initialized) {
            LOG.warn("已经初始化，请勿重复初始化");
            return;
        }
        this.bootstrap = new Bootstrap();
        workerGroup = new NioEventLoopGroup();
        this.bootstrap.group(workerGroup);
        this.bootstrap.channel(NioDatagramChannel.class);
        // 支持广播
        this.bootstrap.option(ChannelOption.SO_BROADCAST, true);
        // 设置UDP读缓冲区为64k
        this.bootstrap.option(ChannelOption.SO_RCVBUF, BUFFER_SIZE);
        // 设置UDP写缓冲区为64k
        this.bootstrap.option(ChannelOption.SO_SNDBUF, BUFFER_SIZE);
        UdpServerHandler udpServerHandler = new UdpServerHandler(clientEntries);
        this.bootstrap.handler(new ChannelInitializer<NioDatagramChannel>() {
            @Override
            protected void initChannel(NioDatagramChannel ch) throws Exception {
                Iterator channelIterator = ((List) UdpServerChannelManager.this.channelSupplier.get()).iterator();
                while (channelIterator.hasNext()) {
                    ChannelHandler handler = (ChannelHandler) channelIterator.next();
                    ch.pipeline().addLast(new ChannelHandler[]{handler});
                }
                if (readTimeout > 0) {
                    ch.pipeline().addLast(new IdleStateHandler(readTimeout, 0, 0, TimeUnit.MILLISECONDS));
                }
                ch.pipeline().addLast(udpServerHandler);
                ch.pipeline().addLast(new ServerConnectionStateNotifier(clientEntries));
            }
        });
        try {
            // 当没有host时，返回默认的0.0.0.0作为地址
            channelFuture = bootstrap.bind(host, port).sync();
            channelFuture.addListener((ChannelFuture future) -> {
                if (future.isSuccess()) {
                    initialized = true;
                    throw new RobotException("udp绑定[" + host + ":" + port + "]成功");
                } else {
                    throw new RobotException("udp绑定[" + host + ":" + port + "]不成功");
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            throw new RobotException("UdpServerChannelManager initialized is " + isInitialized() + ", error message:  " + e.getMessage());
        }
    }

    public boolean isInitialized() {
        return initialized;
    }

    /**
     * 终止
     */
    public void terminate() {
        if (!initialized) {
            return;
        }
        channelFuture.channel().close();
        channelFuture = null;
        bootstrap.config().group().shutdownGracefully();
        initialized = false;
    }

    public void register(ClientEntry clientEntry) {
        if (!initialized) {
            throw new IllegalArgumentException("没有初始化");
        }
        String key = clientEntry.getKey();
        if (clientEntries.containsKey(key)) {
            LOG.warn("该客户端[{}] 已经存在，不能重复注册", key);
            return;
        }
        LOG.info("注册客户端[{}]成功, endPoint: {}:{}", key, clientEntry.getHost(), clientEntry.getPort());
        clientEntry.setChannel(channelFuture.channel());
        clientEntries.put(key, clientEntry);
    }

    public void unregister(Object key) {
        if (!initialized) {
            throw new IllegalArgumentException("没有初始化");
        }

        ClientEntry client = clientEntries.remove(key);
        if (client != null) {
            client.disconnect();
        }
    }

    public void reregister(String key) {
        ClientEntry clientEntry = clientEntries.get(key);
        if (null == clientEntry) {
            throw new NullPointerException("根据[" + key + "]查找不到对应的ClientEntry对象");
        }
        String host = clientEntry.getHost();
        int port = clientEntry.getPort();
        ConnectionEventListener connectionEventListener = clientEntry.getConnectionEventListener();
        unregister(key);
        register(new ClientEntry(key, host, port, connectionEventListener));
    }

    public void closeClientConnection(Object key) {
        if (!initialized) {
            throw new IllegalArgumentException("没有初始化");
        }

        if (isClientConnected(key)) {
            LOG.debug("Closing connection to client {}", key);
            clientEntries.get(key).getChannel().disconnect();
            clientEntries.get(key).setChannel(null);
        }
    }

    public void setLoggingEnabled(Object key, boolean enabled) {
        if (!initialized) {
            throw new IllegalArgumentException("没有初始化");
        }

        ClientEntry entry = clientEntries.get(key);
        if (entry == null) {
            new NullPointerException("No client registered for key: " + key);
        }

        Channel channel = entry.getChannel();
        if (channel == null) {
            LOG.debug("No channel/pipeline for key '%s', doing nothing.");
            return;
        }

        ChannelPipeline pipeline = channel.pipeline();
        if (enabled && pipeline.get(LOGGING_HANDLER_NAME) == null) {
            pipeline.addFirst(LOGGING_HANDLER_NAME,
                    new LoggingHandler(UdpServerChannelManager.this.getClass()));
        } else if (!enabled && pipeline.get(LOGGING_HANDLER_NAME) != null) {
            pipeline.remove(LOGGING_HANDLER_NAME);
        }
    }

    public boolean isClientConnected(Object key) {
        return channelFuture != null
                && clientEntries.containsKey(key)
                && clientEntries.get(key).getChannel() != null
                && clientEntries.get(key).getChannel().isActive();
    }

    public void send(String key, O telegram) {
        if (!initialized) {
            throw new IllegalArgumentException("没有初始化.");
        }

        if (!isClientConnected(key)) {
            LOG.warn("发送报文[{}]失败. [{}]没有链接成功.", telegram, key);
            return;
        }
        ClientEntry clientEntry = clientEntries.get(key);
        InetSocketAddress address = new InetSocketAddress(clientEntry.getHost(), clientEntry.getPort());
        String telegramStr = telegram.toString();
        LOG.info("upd server send telegram[{}] to client[{}:{}]", telegramStr, address.getHostString(), address.getPort());
        clientEntry.getChannel().writeAndFlush(new DatagramPacket(Unpooled.copiedBuffer(telegramStr, CharsetUtil.UTF_8), address));
    }
}
