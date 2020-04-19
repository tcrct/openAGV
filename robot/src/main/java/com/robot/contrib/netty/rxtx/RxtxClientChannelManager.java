package com.robot.contrib.netty.rxtx;

import com.robot.contrib.netty.ConnectionEventListener;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.oio.OioEventLoopGroup;
import io.netty.channel.rxtx.RxtxChannel;
import io.netty.channel.rxtx.RxtxChannelOption;
import io.netty.channel.rxtx.RxtxDeviceAddress;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.concurrent.ScheduledFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import static com.google.common.base.Preconditions.checkState;
import static java.util.Objects.requireNonNull;

/**
 * RXTX渠道管理器
 *
 * @author Laotang
 * @date 2020-02-19
 * @since 1.0
 */
public class RxtxClientChannelManager {

    private static final Logger LOG = LoggerFactory.getLogger(RxtxClientChannelManager.class);

    private final ConnectionEventListener connectionEventListener;
    private final Supplier<List<ChannelHandler>> channelSupplier;
    private ChannelFuture channelFuture;
    private boolean initialized;
    private int readTimeout;
    private ScheduledFuture<?> connectFuture;
    private boolean loggingEnabled;
    private Bootstrap bootstrap;
    private OioEventLoopGroup workerGroup;
    private static final String LOGGING_HANDLER_NAME = "RxtxChannelLoggingHandler";

    public RxtxClientChannelManager(@Nonnull ConnectionEventListener connectionEventListener,
                                    Supplier<List<ChannelHandler>> channelSupplier,
                                    int readTimeout,
                                    boolean enableLogging) {
        this.connectionEventListener = requireNonNull(connectionEventListener, "connEventListener");
        this.channelSupplier = requireNonNull(channelSupplier, "channelSupplier");
        this.readTimeout = readTimeout;
        this.loggingEnabled = enableLogging;
    }

    public void initialize() {
        if (this.initialized) {
            LOG.warn("已经初始化，请勿重复初始化");
            return;
        }
        RxtxClientHandler rxtxHandler = new RxtxClientHandler(connectionEventListener);
        this.bootstrap = new Bootstrap();
        workerGroup = new OioEventLoopGroup();
        this.bootstrap.group(workerGroup)
                .channel(RxtxChannel.class)
                .handler(new ChannelInitializer<RxtxChannel>() {
                    @Override
                    protected void initChannel(RxtxChannel ch) throws Exception {
                        if (loggingEnabled) {
                            ch.pipeline().addFirst(LOGGING_HANDLER_NAME,
                                    new LoggingHandler(RxtxClientChannelManager.this.getClass()));
                        }
                        if (readTimeout > 0) {
                            ch.pipeline().addLast(new IdleStateHandler(readTimeout, 0, 0, TimeUnit.MILLISECONDS));
                        }
                        for (ChannelHandler handler : channelSupplier.get()) {
                            ch.pipeline().addLast(handler);
                        }
                        ch.pipeline().addLast(rxtxHandler);
                    }
                });
        initialized = true;
    }

    public void connect(@Nonnull String host, int port) {
        requireNonNull(host, "host");
        checkState(isInitialized(), "Not initialized");
        if (isConnected()) {
            LOG.debug("Already connected, doing nothing.");
            return;
        }
        try {
            bootstrap.option(RxtxChannelOption.BAUD_RATE, port);
            channelFuture = bootstrap.connect(new RxtxDeviceAddress(host)).sync();
            channelFuture.addListener((ChannelFuture future) -> {
                if (future.isSuccess()) {
                    this.initialized = true;
                    LOG.info("串口连接并监听成功，名称[{}]，波特率[{}]", host, port);
                    connectionEventListener.onConnect();
                } else {
                    connectionEventListener.onFailedConnectionAttempt();
                    LOG.info("打开串口时失败，名称[" + host + "]， 波特率[" + port + "], 串口可能已被占用！");
                }
            });
            connectFuture = null;
        } catch (Exception e) {
            e.printStackTrace();
            workerGroup.shutdownGracefully();
            throw new RuntimeException("RxtxClientChannelManager initialized is " + isInitialized() + ", exception message:  " + e.getMessage());
        }
    }

    public boolean isInitialized() {
        return initialized;
    }

    public boolean isConnected() {
        return channelFuture != null && channelFuture.channel().isActive();
    }

    public void disconnect() {
        if (!isConnected()) {
            return;
        }
        if (channelFuture != null) {
            channelFuture.channel().disconnect();
            channelFuture = null;
        }
    }

    public void scheduleConnect(@Nonnull String host, int port, long delay) {
        requireNonNull(host, "host");
        checkState(isInitialized(), "Not initialized");
        checkState(connectFuture == null, "Connection attempt already scheduled");

        connectFuture = workerGroup.schedule(() -> connect(host, port), delay, TimeUnit.MILLISECONDS);
    }

    public void setLoggingEnabled(boolean enabled) {
        checkState(initialized, "Not initialized.");

        if (channelFuture == null) {
            LOG.debug("No channel future available, doing nothing.");
            return;
        }

        ChannelPipeline pipeline = channelFuture.channel().pipeline();
        if (enabled && pipeline.get(LOGGING_HANDLER_NAME) == null) {
            pipeline.addFirst(LOGGING_HANDLER_NAME,
                    new LoggingHandler(RxtxClientChannelManager.this.getClass()));
        } else if (!enabled && pipeline.get(LOGGING_HANDLER_NAME) != null) {
            pipeline.remove(LOGGING_HANDLER_NAME);
        }
    }

    public void cancelConnect() {
        if (connectFuture == null) {
            return;
        }
        connectFuture.cancel(false);
        connectFuture = null;
    }

    public void terminate() {
        if (!initialized) {
            return;
        }

        cancelConnect();
        disconnect();
        workerGroup.shutdownGracefully();
        workerGroup = null;
        bootstrap = null;

        initialized = false;
    }

    /**
     * 发送报文
     *
     * @param telegram
     * @throws Exception
     */
    public void send(String telegram) throws Exception {
        channelFuture.channel().writeAndFlush(telegram);
        LOG.info("send telegram: {}", telegram);
    }
}

