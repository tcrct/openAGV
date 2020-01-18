package com.openagv.contrib.netty.udp;

import cn.hutool.core.util.ObjectUtil;
import com.openagv.adapter.AgvCommAdapter;
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
 *  @param <O> 发送到车辆或设备的对象
 * @param <I> 接收车辆或设备提交的对象
 */
public class UdpClientChannelManager<O, I> {

    private static final Logger LOG = LoggerFactory.getLogger(UdpClientChannelManager.class);

    private Bootstrap bootstrap;
    private EventLoopGroup workerGroup;
    private ChannelFuture channelFuture;
    private ScheduledFuture<?> connectFuture;
    private AgvCommAdapter adapter;
    private Supplier<List<ChannelHandler>> channelSupplier;
    private String host;
    private int port;
    /**读超时*/
    private int readTimeout;
    /**开启日志**/
    private boolean enableLogging;

    /**是否已经初始化*/
    private boolean initialized;
    /**缓冲大小*/
    private static int BUFFER_SIZE = 64 * 1024;
    private static final String LOGGING_HANDLER_NAME = "ChannelLoggingHandler";

    public UdpClientChannelManager(@Nonnull AgvCommAdapter adapter,
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
        this.workerGroup = new NioEventLoopGroup();
        this.bootstrap.group(workerGroup);
        this.bootstrap.channel(NioDatagramChannel.class);
        // 支持广播
        this.bootstrap.option(ChannelOption.SO_BROADCAST, true);
        // 设置UDP读缓冲区为64k
        this.bootstrap.option(ChannelOption.SO_RCVBUF, BUFFER_SIZE);
        // 设置UDP写缓冲区为64k
        this.bootstrap.option(ChannelOption.SO_SNDBUF, BUFFER_SIZE);
        UdpClientHandler udpHandler = new UdpClientHandler(this, adapter);
        this.bootstrap.handler(new ChannelInitializer<NioDatagramChannel>() {
            @Override
            protected void initChannel(NioDatagramChannel ch) throws Exception {
                Iterator channelIterator = ((List) UdpClientChannelManager.this.channelSupplier.get()).iterator();
                while (channelIterator.hasNext()) {
                    ChannelHandler handler = (ChannelHandler) channelIterator.next();
                    ch.pipeline().addLast(new ChannelHandler[]{handler});
                }
                ch.pipeline().addLast(udpHandler);
            }
        });
        initialized = true;
    }

    /**是否初始化*/
    public boolean isInitialized() {
        return initialized;
    }

    /**断开链接*/
    public void terminate() {
        disconnect();
    }

    /**
     * 根据host与port链接客户端
     * @param host 客户端地址
     * @param port 客户端端口
     * @throws InterruptedException
     */
    public void connect(String host, int port) throws InterruptedException {
        this.host = requireNonNull(host, "host");
        this.port = port;
        checkState(isInitialized(), "UDP服务没有初始化");
        if (isConnected()) {
            LOG.info("请勿重复连接");
            return;
        }
        LOG.warn("正在启动连接尝试{}:{}...", host, port);
        channelFuture = bootstrap.bind(host, port).sync();
        channelFuture.addListener((ChannelFuture future) -> {
            if (future.isSuccess()) {
                this.initialized = true;
                LOG.warn("UdpClientChannelManager链接城功:  {}:{}", host, port);
            }
            else {
                throw new InterruptedException("UdpClientChannelManager链接失败");
            }
        });
        connectFuture = null;
    }

    /**断开链接*/
    public void disconnect() {
        if (!this.initialized) {
            LOG.warn("UdpClientChannelManager没有执行初始化操作!");
            return;
        }
        if (channelFuture != null) {
            this.channelFuture.channel().close();
            this.bootstrap.config().group().shutdownGracefully();
            this.initialized = false;
            this.channelFuture = null;
            LOG.warn("UdpClientChannelManager断开连接!");
        }
    }

    /**是否链接*/
    public boolean isConnected() {
        return channelFuture != null && channelFuture.channel().isActive();
    }

    /**日志开启*/
    public void setLoggingEnabled(boolean enabled) {
        checkState(initialized, "Not initialized.");

        if (null == channelFuture) {
            LOG.warn("No channel future available, doing nothing.");
            return;
        }

        ChannelPipeline pipeline = channelFuture.channel().pipeline();
        if (enabled && pipeline.get(LOGGING_HANDLER_NAME) == null) {
            pipeline.addFirst(LOGGING_HANDLER_NAME,
                    new LoggingHandler(UdpClientChannelManager.this.getClass()));
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
            throw new IllegalArgumentException("没有初始化.");
        }
        if (ObjectUtil.isEmpty(telegram)) {
            throw new IllegalArgumentException("广播的报文内容不能为空");
        }

        String host= adapter.getProcessModel().getVehicleHost();
        int port = adapter.getProcessModel().getVehiclePort();
        InetSocketAddress address = new InetSocketAddress(host, port);
        String telegramStr = telegram.toString();
        LOG.info("send upd client [{}][{}], telegram [{}] ",
                adapter.getProcessModel().getName(),
                (address.getAddress().toString()+":"+address.getPort()),
                telegramStr);
        channelFuture.channel().writeAndFlush(new DatagramPacket(Unpooled.copiedBuffer(telegramStr, CharsetUtil.UTF_8),  address));
    }


}
