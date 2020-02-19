package com.robot.contrib.netty.rxtx;

import com.robot.contrib.netty.comm.AbstractServerChannelManager;
import com.robot.contrib.netty.comm.ClientEntry;
import com.robot.contrib.netty.comm.ServerConnectionStateNotifier;
import com.robot.contrib.netty.tcp.TcpServerChannelManager;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.oio.OioEventLoopGroup;
import io.netty.channel.rxtx.RxtxChannel;
import io.netty.channel.rxtx.RxtxChannelOption;
import io.netty.channel.rxtx.RxtxDeviceAddress;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 *RXTX渠道管理器
 *
 * @author Laotang
 * @date 2020-02-19
 * @since 1.0
 */
public class RxtxServerChannelManager extends AbstractServerChannelManager {

    private static final Logger LOG = LoggerFactory.getLogger(RxtxServerChannelManager.class);

    private static final String LOGGING_HANDLER_NAME = "RxtxChannelLoggingHandler";

    public RxtxServerChannelManager(String serialport, int baudrate,
                                    Map<String, ClientEntry> clientEntries,
                                    Supplier<List<ChannelHandler>> channelSupplier,
                                    int readTimeout,
                                    boolean loggingInitially) {
        super(serialport, baudrate, clientEntries, channelSupplier, readTimeout, loggingInitially);
    }

    public void initialize() {
        if (this.initialized) {
            LOG.warn("已经初始化，请勿重复初始化");
            return;
        }
        RxtxServerHandler rxtxHandler = new RxtxServerHandler(clientEntries);
        this.bootstrap = new Bootstrap();
        OioEventLoopGroup workerGroup = new OioEventLoopGroup();
        this.bootstrap.group(workerGroup)
                .channel(RxtxChannel.class)
                .handler(new ChannelInitializer<RxtxChannel>() {
                    @Override
                    protected void initChannel(RxtxChannel ch) throws Exception {
                        if (loggingInitially) {
                            ch.pipeline().addFirst(LOGGING_HANDLER_NAME,
                                    new LoggingHandler(RxtxServerChannelManager.this.getClass()));
                        }
                        if (readTimeout > 0) {
                            ch.pipeline().addLast(new IdleStateHandler(readTimeout, 0, 0, TimeUnit.MILLISECONDS));
                        }
                        for (ChannelHandler handler : channelSupplier.get()) {
                            ch.pipeline().addLast(handler);
                        }
                        ch.pipeline().addLast(rxtxHandler);
                        ch.pipeline().addLast(new ServerConnectionStateNotifier(clientEntries));
                    }
                });
        // RxtxChannel rxtxChannel = new RxtxChannel();
        bootstrap.option(RxtxChannelOption.BAUD_RATE, port);
        try {
            serverChannelFuture = bootstrap.connect(new RxtxDeviceAddress(host)).sync();
            serverChannelFuture.addListener((ChannelFuture future) -> {
                if (future.isSuccess()) {
                    this.initialized = true;
                    LOG.info("串口连接并监听成功，名称[{}]，波特率[{}]", host, port);
                } else {
                    throw new RuntimeException("打开串口时失败，名称[" + host + "]， 波特率[" + port + "], 串口可能已被占用！");
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            workerGroup.shutdownGracefully();
            throw new RuntimeException("RxtxServerChannelManager initialized is " + isInitialized() + ", exception message:  " + e.getMessage());
        }
    }

    @Override
    public void setLoggingEnabled(String key, boolean enable) {
        setLoggingEnabled(key, enable, TcpServerChannelManager.class, LOGGING_HANDLER_NAME);
    }

    @Override
    public void send(String key, String message) throws Exception {
        super.send(key, (Object) message);
        LOG.info("send to client[{}], message: {}", message, key);
    }
}

