package com.robot.contrib.netty.udp;

import com.robot.contrib.netty.ConnectionEventListener;
import com.robot.contrib.netty.comm.AbstractServerChannelManager;
import com.robot.contrib.netty.comm.ClientEntry;
import com.robot.contrib.netty.comm.ServerConnectionStateNotifier;
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
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;


/**
 * Udp通讯服务
 *
 * @author Laotang
 * @since 1.0
 */
public class UdpServerChannelManager extends AbstractServerChannelManager {

    private static final Logger LOG = LoggerFactory.getLogger(UdpServerChannelManager.class);

    private static int BUFFER_SIZE = 64 * 1024;
    private static final String LOGGING_HANDLER_NAME = "UdpChannelLoggingHandler";

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
    public UdpServerChannelManager(String host, int port,
                                   Map<String, ClientEntry> clientEntries,
                                   Supplier<List<ChannelHandler>> channelSupplier,
                                   int readTimeout,
                                   boolean loggingInitially) {
        super(host, port, clientEntries, channelSupplier, readTimeout, loggingInitially);
    }

    /**
     *  初始化,进行地址端口绑定
     */
    @Override
    public void initialize() {
        if (super.initialized) {
            LOG.warn("已经初始化，请勿重复初始化");
            return;
        }
        bootstrap = new Bootstrap();
        NioEventLoopGroup workerGroup = new NioEventLoopGroup();
        bootstrap.group(workerGroup);
        bootstrap.channel(NioDatagramChannel.class);
        // 支持广播
        bootstrap.option(ChannelOption.SO_BROADCAST, true);
        // 设置UDP读缓冲区为64k
        bootstrap.option(ChannelOption.SO_RCVBUF, BUFFER_SIZE);
        // 设置UDP写缓冲区为64k
        bootstrap.option(ChannelOption.SO_SNDBUF, BUFFER_SIZE);
        UdpServerHandler udpServerHandler = new UdpServerHandler(clientEntries);
        bootstrap.handler(new ChannelInitializer<NioDatagramChannel>() {
            @Override
            protected void initChannel(NioDatagramChannel ch) throws Exception {
                if (loggingInitially) {
                    ch.pipeline().addFirst(LOGGING_HANDLER_NAME,
                            new LoggingHandler(UdpServerChannelManager.this.getClass()));
                }
                if (readTimeout > 0) {
                    ch.pipeline().addLast(new IdleStateHandler(readTimeout, 0, 0, TimeUnit.MILLISECONDS));
                }
                for (ChannelHandler handler : channelSupplier.get()) {
                    ch.pipeline().addLast(handler);
                }
                ch.pipeline().addLast(udpServerHandler);
                ch.pipeline().addLast(new ServerConnectionStateNotifier(clientEntries));
            }
        });
        try {
            // 当没有host时，返回默认的0.0.0.0作为地址
            serverChannelFuture = bootstrap.bind(host, port).sync();
            serverChannelFuture.addListener((ChannelFuture future) -> {
                if (future.isSuccess()) {
                    initialized = true;
                    LOG.warn("UdpServerChannelManager绑定[" + host + ":" + port + "]成功");
                } else {
                    throw new RuntimeException("udp绑定[" + host + ":" + port + "]不成功");
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            workerGroup.shutdownGracefully();
            throw new RuntimeException("UdpServerChannelManager initialized is " + isInitialized() + ", exception message:  " + e.getMessage());
        }
    }

    @Override
    public void setLoggingEnabled(String key, boolean enabled) {
        super.setLoggingEnabled(key, enabled, UdpServerChannelManager.class, LOGGING_HANDLER_NAME);
    }

    /**
     * 发送消息
     * @param key 客户端关键字，须保证唯一
     * @param message 发送的内容
     */
    @Override
    public void send(String key, String message) throws Exception {
        ClientEntry clientEntry = clientEntries.get(key);
        InetSocketAddress address = new InetSocketAddress(clientEntry.getHost(), clientEntry.getPort());
        super.send(key, new DatagramPacket(Unpooled.copiedBuffer(message, CharsetUtil.UTF_8), address));
        LOG.info("send to client[{}:{}], message: {}", message, clientEntry.getHost(), clientEntry.getPort());
    }
}
