/**
 * Copyright (c) The openTCS Authors.
 * <p>
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package com.robot.contrib.netty.tcp;

import com.robot.contrib.netty.ConnectionEventListener;
import com.robot.contrib.netty.comm.ClientEntry;
import com.robot.contrib.netty.comm.ServerConnectionStateNotifier;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;

/**
 * Manages a TCP connection to a peer, with the peer being the client.
 * <p>
 * Note that the name of the {@link LoggingHandler} that this class optionally registers is this
 * class's fully qualified name.
 * </p>
 *
 * @param <I> The type of incoming messages on this TcpServerChannelManager.
 * @param <O> The type of outgoing messages on this TcpServerChannelManager.
 * @author Martin Grzenia (Fraunhofer IML)
 * @author Stefan Walter (Fraunhofer IML)
 */
public class TcpServerChannelManager<I, O> {

    /**
     * This class's Logger.
     */
    private static final Logger LOG = LoggerFactory.getLogger(TcpServerChannelManager.class);
    /**
     * The name for logging handlers.
     */
    private static final String LOGGING_HANDLER_NAME = "ChannelLoggingHandler";
    /**
     * Bootstraps the channel.
     */
    private ServerBootstrap bootstrap;

    /**
     * The host to listen on.
     */
    private String host = "0.0.0.0";
    /**
     * The port to listen on.
     */
    private int port = 7070;
    /**
     * A pool of clients that may connect to this manager.
     */
    private final Map<Object, ClientEntry> clientEntries;
    /**
     * A supplier for lists of {@link ChannelHandler} instances to be added to the pipeline of each
     * new connection.
     */
    private final Supplier<List<ChannelHandler>> channelSupplier;
    /**
     * Whether to enable logging for channels initially.
     */
    private final boolean loggingInitially;
    /**
     * Manages the server channel.
     */
    private ChannelFuture serverChannelFuture;
    /**
     * Whether this component is initialized or not.
     */
    private boolean initialized;
    /**
     * The read timeout (in milliseconds). Zero if disabled.
     */
    private final int readTimeout;

    /**
     * Creates a new instance.
     *
     * @param clientEntries    Entries for clients accepting connections via this channel manager.
     * @param channelSupplier  A supplier for lists of {@link ChannelHandler} instances that should be
     *                         added to the pipeline of each new connection.
     * @param readTimeout      A timeout in milliseconds after which a connection should be closed if no
     *                         data was received over it. May be zero to disable.
     * @param loggingInitially Whether to turn on logging by default for new connections.
     */
    public TcpServerChannelManager(
            Map<Object, ClientEntry> clientEntries,
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
        if (initialized) {
            LOG.warn("已经初始化，请勿重复初始化");
            return;
        }

        bootstrap = new ServerBootstrap();
        bootstrap.group(new NioEventLoopGroup(), new NioEventLoopGroup());
        bootstrap.channel(NioServerSocketChannel.class);
        bootstrap.option(ChannelOption.SO_BACKLOG, 1);
        bootstrap.childOption(ChannelOption.SO_KEEPALIVE, true);
        bootstrap.childOption(ChannelOption.TCP_NODELAY, true);
        bootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel ch) {
                if (loggingInitially) {
                    ch.pipeline().addFirst(LOGGING_HANDLER_NAME,
                            new LoggingHandler(TcpServerChannelManager.this.getClass()));
                }
                if (readTimeout > 0) {
                    ch.pipeline().addLast(new IdleStateHandler(readTimeout, 0, 0, TimeUnit.MILLISECONDS));
                }
                for (ChannelHandler handler : channelSupplier.get()) {
                    ch.pipeline().addLast(handler);
                }
                ch.pipeline().addLast(new ServerConnectionStateNotifier<>(clientEntries));
            }

        });
        try {
            serverChannelFuture = bootstrap.bind(host, port).sync();
            initialized = true;
            LOG.warn("TcpServerChannelManager initialized is {}", isInitialized());
        } catch (Exception e) {
            e.printStackTrace();
            LOG.warn("TcpServerChannelManager initialized is {}, error message:  {}", isInitialized(), e.getMessage());
        }
    }

    /**
     * Disconnects the channel if it is connected and frees all resources.
     * This method should be called when this instance isn't needed any more. Once it has been called,
     * the behaviour of all other methods is undefined.
     */
    public void terminate() {
        if (!initialized) {
            return;
        }

        serverChannelFuture.channel().close();
        serverChannelFuture = null;
        for (ClientEntry clientEntry : clientEntries.values()) {
            clientEntry.disconnect();
        }
        clientEntries.clear();
        bootstrap.config().group().shutdownGracefully();
        bootstrap.config().childGroup().shutdownGracefully();

        initialized = false;
    }

    public boolean isInitialized() {
        return initialized;
    }

    public void register(ClientEntry clientEntry) {
        if (!initialized) {
            throw new IllegalArgumentException("Not initialized.");
        }

        String key = clientEntry.getKey();
        if (clientEntries.containsKey(key)) {
            LOG.warn("A handler for '{}' is already registered.", key);
            return;
        }

        LOG.info("注册客户端[{}]成功, endPoint: {}:{}", key, clientEntry.getHost(), clientEntry.getPort());
        clientEntry.setChannel(serverChannelFuture.channel());
        clientEntries.put(key, clientEntry);
    }

    public void unregister(Object key) {
        if (!initialized) {
            throw new IllegalArgumentException("Not initialized.");
        }

        ClientEntry client = clientEntries.remove(key);
        if (client != null) {
            client.disconnect();
        }
    }

    public void reregister(String key) {
        ClientEntry clientEntry = clientEntries.get(key);
        if (null == clientEntry) {
            throw new NullPointerException("clientEntries根据[" + key + "]查找不到对应的ClientEntry对象");
        }
        String host = clientEntry.getHost();
        int port = clientEntry.getPort();
        ConnectionEventListener<I> connectionEventListener = clientEntry.getConnectionEventListener();
        unregister(key);
        register(new ClientEntry(key, host, port, connectionEventListener));
    }

    public void closeClientConnection(Object key) {
        if (!initialized) {
            throw new IllegalArgumentException("Not initialized.");
        }

        if (isClientConnected(key)) {
            LOG.debug("Closing connection to client {}", key);
            clientEntries.get(key).getChannel().disconnect();
            clientEntries.get(key).setChannel(null);
        }
    }

    /**
     * Checks whether a connection to the given client exists.
     *
     * @param key The key associated with the client.
     * @return <code>true</code> if, and only if, a connection has been initiated and is active.
     */
    public boolean isClientConnected(Object key) {
        return serverChannelFuture != null
                && clientEntries.containsKey(key);
//                && clientEntries.get(key).getChannel() != null
//                && clientEntries.get(key).getChannel().isActive();
    }

    /**
     * Encodes and sends a telegram to the peer, if connected.
     *
     * @param telegram The telegram.
     * @param key      The key associated to the client the telegram should be sent to.
     */
    public void send(Object key, O telegram) {
        if (!initialized) {
            throw new IllegalArgumentException("Not initialized.");
        }

        if (!isClientConnected(key)) {
            LOG.warn("Failed sending telegram {}. {} is not connected.", telegram, key);
            return;
        }
        LOG.debug("Sending telegram {} to {}.", telegram, key);

        clientEntries.get(key).getChannel().writeAndFlush(telegram);
    }

    /**
     * Enables or disables logging for the client entry with the registered key.
     *
     * @param key     The key identifying the client entry.
     * @param enabled Indicates whether to enable or disable logging for the client entry.
     */
    public void setLoggingEnabled(Object key, boolean enabled) {
        if (!initialized) {
            throw new IllegalArgumentException("Not initialized.");
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
                    new LoggingHandler(TcpServerChannelManager.this.getClass()));
        } else if (!enabled && pipeline.get(LOGGING_HANDLER_NAME) != null) {
            pipeline.remove(LOGGING_HANDLER_NAME);
        }
    }

    /**
     * Returns the port on which this channel manager listens on for incoming connections.
     *
     * @return The port.
     */
    public int getPort() {
        return port;
    }

    /**
     * 读超时，毫秒作单位
     *
     * @return The read timeout in milliseconds. Zero if disabled.
     */
    public int getReadTimeout() {
        return readTimeout;
    }
}
