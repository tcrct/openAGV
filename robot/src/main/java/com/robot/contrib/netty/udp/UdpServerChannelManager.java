package com.robot.contrib.netty.udp;

import cn.hutool.core.util.ObjectUtil;
import com.robot.adapter.RobotCommAdapter;
import com.robot.contrib.netty.tcp.ClientEntry;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.CharsetUtil;
import io.netty.util.concurrent.ScheduledFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.net.InetSocketAddress;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;
import static org.opentcs.util.Assertions.checkState;

/**
 * Created by laotang on 2019/12/21.
 *
 * @param <I> The type of incoming messages on this UdpServerChannelManager.
 *  @param <O> The type of outgoing messages on this UdpServerChannelManager.
 */
public class UdpServerChannelManager<I, O> {

    private static final Logger LOG = LoggerFactory.getLogger(UdpServerChannelManager.class);

    private Bootstrap bootstrap;
    private EventLoopGroup workerGroup;
    private ChannelFuture channelFuture;
    private ScheduledFuture<?> connectFuture;
    private RobotCommAdapter adapter;
    private Supplier<List<ChannelHandler>> channelSupplier;
    private int readTimeout;
    private boolean enableLogging;

    private boolean initialized;
    private static int BUFFER_SIZE = 64 * 1024;
    private static final String LOGGING_HANDLER_NAME = "ChannelLoggingHandler";

    public UdpServerChannelManager(@Nonnull RobotCommAdapter adapter,
                                   Supplier<List<ChannelHandler>> channelSupplier,
                                   int readTimeout,
                                   boolean enableLogging) {
        this.adapter = adapter;
        this.channelSupplier = channelSupplier;
        this.readTimeout = readTimeout;
        this.enableLogging = enableLogging;
    }

    public void initialized() {
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
        UdpServerHandler udpServerHandler = new UdpServerHandler();
        this.bootstrap.handler(new ChannelInitializer<NioDatagramChannel>() {
            @Override
            protected void initChannel(NioDatagramChannel ch) throws Exception {
                Iterator channelIterator = ((List) UdpServerChannelManager.this.channelSupplier.get()).iterator();
                while (channelIterator.hasNext()) {
                    ChannelHandler handler = (ChannelHandler) channelIterator.next();
                    ch.pipeline().addLast(new ChannelHandler[]{handler});
                }
                ch.pipeline().addLast(udpServerHandler);
            }
        });
        initialized = true;
        LOG.warn("UdpServerChannelManager initialized is {}", isInitialized());
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

    public void connect(String host, int port) throws InterruptedException {
        requireNonNull(host, "host");
        if (!isInitialized()) {
            throw new InterruptedException("Not initialized");
        }
        if (isConnected()) {
            throw new InterruptedException("Already connected, doing nothing.");
        }
        channelFuture = bootstrap.bind(host, port).sync();
        channelFuture.addListener((ChannelFuture future) -> {
            if (future.isSuccess()) {
                this.initialized = true;
                LOG.warn("UdpServerChannelManager connect is success:  {}:{}", host, port);
                adapter.onConnect();
            }
            else {
                adapter.onFailedConnectionAttempt();
                LOG.error("UdpServerChannelManager connect fail");
            }
        });
        connectFuture = null;
    } 

    public void disconnect() {
        if (!isInitialized()) {
            LOG.warn("UdpServerChannelManager is not initalized!");
            return;
        }
        if (channelFuture != null) {
            this.channelFuture.channel().close();
            LOG.warn("UdpServerChannelManager is disconnect!");
        }
    }

    public boolean isConnected() {
        return channelFuture != null && channelFuture.channel().isActive();
    }

    public void setLoggingEnabled(boolean enabled) {
        checkState(initialized, "Not initialized.");

        if (null == channelFuture) {
            LOG.warn("No channel future available, doing nothing.");
            return;
        }

        ChannelPipeline pipeline = channelFuture.channel().pipeline();
        if (enabled && pipeline.get(LOGGING_HANDLER_NAME) == null) {
            pipeline.addFirst(LOGGING_HANDLER_NAME,
                    new LoggingHandler(UdpServerChannelManager.this.getClass()));
        }
        else if (!enabled && pipeline.get(LOGGING_HANDLER_NAME) != null) {
            pipeline.remove(LOGGING_HANDLER_NAME);
        }
    }

    public void scheduleConnect(@Nonnull String host, int port, long delay) {
        requireNonNull(host, "host");
        checkState(isInitialized(), "Not initialized");
        checkState(channelFuture == null, "Connection attempt already scheduled");

        connectFuture = workerGroup.schedule(() -> {
            try {
                connect(host, port);
            } catch (InterruptedException e) {
                LOG.info("重连时发生异常： " + e.getMessage(), e);
            }
        }, delay, TimeUnit.MILLISECONDS);
    }

    public void send(O telegram) {
        if (!this.isConnected()) {
            throw new IllegalArgumentException("Not Connected.");
        }
        if (ObjectUtil.isEmpty(telegram)) {
            throw new IllegalArgumentException("广播的报文内容不能为空");
        }

        String host= adapter.getProcessModel().getVehicleHost();
        int port = adapter.getProcessModel().getVehiclePort();
        InetSocketAddress address = new InetSocketAddress(host, port);
        String telegramStr = telegram.toString();
        LOG.info("upd server send client[{}][{}], telegram [{}] ",
                adapter.getProcessModel().getName(),
                (address.getAddress().toString()+":"+address.getPort()),
                telegramStr);
        channelFuture.channel().writeAndFlush(new DatagramPacket(Unpooled.copiedBuffer(telegramStr, CharsetUtil.UTF_8),  address));
    }


}
