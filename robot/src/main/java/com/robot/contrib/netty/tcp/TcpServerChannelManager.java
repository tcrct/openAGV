/**
 * Copyright (c) The openTCS Authors.
 * <p>
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package com.robot.contrib.netty.tcp;

import com.robot.contrib.netty.comm.AbstractServerChannelManager;
import com.robot.contrib.netty.comm.ClientEntry;
import com.robot.contrib.netty.comm.ServerConnectionStateNotifier;
import io.netty.bootstrap.Bootstrap;
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


/**
 * TCP渠道管理器
 *
 * @author Laotang
 * @date 2020-02-19
 * @blame Android Team
 * @since 1.0
 */
public class TcpServerChannelManager extends AbstractServerChannelManager {

    private static final Logger LOG = LoggerFactory.getLogger(TcpServerChannelManager.class);
    private static final String LOGGING_HANDLER_NAME = "TcpChannelLoggingHandler";

    /**
     * 构造方法
     * @param host 服务器地址，默认为 0.0.0.0
     * @param port 服务器端口，默认为 7070
     * @param clientEntries 客户端ClientEntry Map集合
     * @param channelSupplier  handler集合
     * @param readTimeout 读超时
     * @param loggingInitially 是否开启日志
     */
    public TcpServerChannelManager(String host, int port,
                                   Map<String, ClientEntry> clientEntries,
                                   Supplier<List<ChannelHandler>> channelSupplier,
                                   int readTimeout,
                                   boolean loggingInitially) {
        super(host, port, clientEntries, channelSupplier, readTimeout, loggingInitially);
    }

    @Override
    public void initialize() {
        if (initialized) {
            LOG.warn("已经初始化，请勿重复初始化");
            return;
        }

        serverBootstrap = new ServerBootstrap();
        NioEventLoopGroup workerGroup = new NioEventLoopGroup();
        serverBootstrap.group(workerGroup, workerGroup);
        serverBootstrap.channel(NioServerSocketChannel.class);
        serverBootstrap.option(ChannelOption.SO_BACKLOG, 1);
        serverBootstrap.childOption(ChannelOption.SO_KEEPALIVE, true);
        serverBootstrap.childOption(ChannelOption.TCP_NODELAY, true);
//        TcpServerHandler tcpServerHandler = new TcpServerHandler(clientEntries);
        serverBootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
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
                ch.pipeline().addLast(new TcpServerHandler(clientEntries));
                ch.pipeline().addLast(new ServerConnectionStateNotifier(clientEntries));
            }

        });
        try {
            serverChannelFuture = serverBootstrap.bind(host, port).sync();
            serverChannelFuture.addListener((ChannelFuture future) -> {
                if (future.isSuccess()) {
                    initialized = true;
                    LOG.warn("TcpServerChannelManager绑定[" + host + ":" + port + "]成功");
                } else {
                    throw new RuntimeException("udp绑定[" + host + ":" + port + "]不成功");
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            workerGroup.shutdownGracefully();
            throw new RuntimeException("TcpServerChannelManager initialized is " + isInitialized() + ", exception message:  " + e.getMessage());
        }
    }

    @Override
    public void setLoggingEnabled(String key, boolean enable) {
        setLoggingEnabled(key, enable, TcpServerChannelManager.class, LOGGING_HANDLER_NAME);
    }

    @Override
    public void send(String key, String message) throws Exception {
        super.send(key, (Object) message);
        LOG.info("send to client[{}], message: {}", key, message);
    }

}
