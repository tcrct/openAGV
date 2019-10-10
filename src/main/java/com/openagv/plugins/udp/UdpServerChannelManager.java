package com.openagv.plugins.udp;


import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import com.openagv.core.AppContext;
import com.openagv.tools.SettingUtils;
import com.openagv.tools.ToolsKit;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.util.CharsetUtil;

import java.net.InetSocketAddress;
import java.util.Iterator;
import java.util.List;
import java.util.function.Supplier;

public class UdpServerChannelManager<I,O> {

    private static final Log logger = LogFactory.get();

    private static final String LOGGING_HANDLER_NAME = "ChannelLoggingHandler";
    private Bootstrap bootstrap;
    private int port;
    private Supplier<List<ChannelHandler>> channelSupplier;
    private ChannelFuture serverChannelFuture;
    private boolean loggingInitially;
    private boolean initialized;
    private static int BUFFER_SIZE = 64 * 1024;
    private InetSocketAddress address;

    public UdpServerChannelManager(int port, Supplier<List<ChannelHandler>> channelSupplier, boolean loggingInitially) {
        this(port, channelSupplier, loggingInitially, BUFFER_SIZE);
    }

    public UdpServerChannelManager(int port, Supplier<List<ChannelHandler>> channelSupplier, boolean loggingInitially, int bufferSize) {
        java.util.Objects.requireNonNull(channelSupplier, "channelSupplier");
        this.port = port;
        this.channelSupplier = channelSupplier;
        this.loggingInitially = loggingInitially;
        this.BUFFER_SIZE = bufferSize;
    }

    public void initialize() {
        if (!this.initialized) {
            this.bootstrap = new Bootstrap();
            this.bootstrap.group(new NioEventLoopGroup());
            this.bootstrap.channel(NioDatagramChannel.class);
            // 支持广播
            this.bootstrap.option(ChannelOption.SO_BROADCAST, true);
            // 设置UDP读缓冲区为64k
            this.bootstrap.option(ChannelOption.SO_RCVBUF, BUFFER_SIZE);
            // 设置UDP写缓冲区为64k
            this.bootstrap.option(ChannelOption.SO_SNDBUF, BUFFER_SIZE);
            this.bootstrap.handler(new ChannelInitializer<NioDatagramChannel>() {
                @Override
                protected void initChannel(NioDatagramChannel ch) throws Exception {
                    Iterator channelIterator = ((List) UdpServerChannelManager.this.channelSupplier.get()).iterator();
                    while (channelIterator.hasNext()) {
                        ChannelHandler handler = (ChannelHandler) channelIterator.next();
                        ch.pipeline().addLast(new ChannelHandler[]{handler});
                    }
                    ch.pipeline().addLast(new UdpHandler());
                }
            });
            try {
                this.serverChannelFuture = this.bootstrap.bind("0.0.0.0", this.port).sync();
                this.initialized = true;
                logger.info("UdpServerChannelManager initialized is success port: {}", port);
            } catch (Exception e) {
                logger.error("UdpServerChannelManager initialized fail: " + e.getMessage(), e);
                this.initialized = false;
            }
        }
    }

    public boolean isConnected() {
        return serverChannelFuture != null && serverChannelFuture.channel().isActive();
    }

    public void disconnect() {
        if (this.initialized) {
            this.serverChannelFuture.channel().close();
            this.bootstrap.config().group().shutdownGracefully();
            this.initialized = false;
            this.serverChannelFuture = null;
            logger.info("Stop Server!");
        }
    }

    public boolean isInitialized() {
        return this.initialized;
    }

    public void setSendAddress(InetSocketAddress sendAddress){
        this.address = sendAddress;
    }

    public void send(String telegram) {
        if (!this.initialized) {
            throw new IllegalArgumentException("Not initialized.");
        }
//        logger.info("UDP广播的报文:{}", telegram);
        if (ToolsKit.isEmpty(telegram)) {
            throw new IllegalArgumentException("广播的报文内容不能为空");
        }
        if(null == address) {
            String host= AppContext.getCommAdapter().getProcessModel().getVehicleHost();
            int port = AppContext.getCommAdapter().getProcessModel().getVehiclePort();
            address = new InetSocketAddress(host, port);
        }
        logger.info("upd server send client {} ", (address.getAddress().toString()+":"+address.getPort()));
        serverChannelFuture.channel().writeAndFlush(new DatagramPacket(Unpooled.copiedBuffer(telegram, CharsetUtil.UTF_8),  address));
        address = null;
    }
}
