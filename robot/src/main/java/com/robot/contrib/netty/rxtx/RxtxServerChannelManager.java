package com.robot.contrib.netty.rxtx;

import com.robot.contrib.netty.ConnectionEventListener;
import com.robot.contrib.netty.comm.ClientEntry;
import com.robot.contrib.netty.comm.ServerConnectionStateNotifier;
import com.robot.mvc.core.interfaces.IResponse;
import com.robot.mvc.utils.ToolsKit;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.oio.OioEventLoopGroup;
import io.netty.channel.rxtx.RxtxChannel;
import io.netty.channel.rxtx.RxtxChannelOption;
import io.netty.channel.rxtx.RxtxDeviceAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Created by laotang on 2020/1/20.
 */
public class RxtxServerChannelManager {

    private static final Logger LOG = LoggerFactory.getLogger(RxtxServerChannelManager.class);

    private Bootstrap bootstrap;
    private OioEventLoopGroup workerGroup;
    private ChannelFuture channelFuture;
    private RxtxChannel rxtxChannel;
    private int readTimeout;
    private boolean enableLogging;
    /**
     * 是否已经初始化
     */
    private boolean initialized;
    private boolean connected;
    private Supplier<List<ChannelHandler>> channelSupplier;
    private String serialport = "COM3";
    private int baudrate = 38400;
    private Map<Object, ClientEntry> clientEntries;

    public RxtxServerChannelManager(
            Map<Object, ClientEntry> clientEntries,
                                    Supplier<List<ChannelHandler>> channelSupplier,
                                    int readTimeout,
                                    boolean enableLogging) {
        this.channelSupplier = channelSupplier;
        this.readTimeout = readTimeout;
        this.enableLogging = enableLogging;
        this.clientEntries = clientEntries;
    }

    public void bind(String serialport, int baudrate) {
        this.serialport = serialport;
        this.baudrate = baudrate;
    }

    public void initialize() {
        if (this.initialized) {
            LOG.warn("已经初始化，请勿重复初始化");
            return;
        }
        try {
            RxtxServerHandler rxtxHandler = new RxtxServerHandler();
            this.bootstrap = new Bootstrap();
            this.workerGroup = new OioEventLoopGroup();
            this.bootstrap.group(workerGroup)
                    .channel(RxtxChannel.class)
                    .handler(new ChannelInitializer<RxtxChannel>() {
                        @Override
                        protected void initChannel(RxtxChannel ch) throws Exception {
                            Iterator channelIterator = ((List) channelSupplier.get()).iterator();
                            while (channelIterator.hasNext()) {
                                ChannelHandler handler = (ChannelHandler) channelIterator.next();
                                ch.pipeline().addLast(new ChannelHandler[]{handler});
                            }
//                            ch.pipeline().addLast(new LengthFieldBasedFrameDecoder(AP_MSG_MAX_LENGTH, 6, 1, 0, 0, true));
                            ch.pipeline().addLast(rxtxHandler);
                            ch.pipeline().addLast(new ServerConnectionStateNotifier<>(clientEntries));
                        }
                    });
            rxtxChannel = new RxtxChannel();
            bootstrap.option(RxtxChannelOption.BAUD_RATE, baudrate);
            channelFuture = bootstrap.connect(new RxtxDeviceAddress(serialport)).sync();
            channelFuture.addListener((ChannelFuture future) -> {
                if (future.isSuccess()) {
                    this.connected = true;
                    LOG.info("串口连接并监听成功，名称[{}]，波特率[{}]", serialport, baudrate);
                } else {
                    throw new RuntimeException("打开串口时失败，名称[" + serialport + "]， 波特率[" + baudrate + "], 串口可能已被占用！");
                }
            });
            initialized = true;
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            workerGroup.shutdownGracefully();
        }
    }

    public boolean isInitialized() {
        return initialized;
    }

    public void terminate() {
        if (isInitialized()) {
            workerGroup.shutdownGracefully();
            this.channelFuture.channel().close();
            this.initialized = false;
            this.channelFuture = null;
            bootstrap = null;
            LOG.warn("RxtxServerChannelManager is terminate!");
        }
    }

    public void register(ClientEntry clientEntry) {
        if (!isInitialized()) {
            throw new RuntimeException("指定的串口初始化不成功");
        }
        if (baudrate == 0) {
            throw new IllegalArgumentException("串口波特率[" + baudrate + "]没有设置");
        }
        LOG.info("注册客户端[{}]成功!", clientEntry.getKey());
        clientEntry.getConnectionEventListener().onConnect();
    }

    public void disconnect(String key) {
        terminate();
    }

    public boolean isConnected(String key) {
        return connected;
    }

    public void setLoggingEnabled(String key, boolean enable) {
        LOG.info("该功能未实现");
    }

    public void scheduleConnect(@Nonnull String host, int port, long delay) {
        LOG.info("该功能未实现");
    }

    public void send(IResponse telegram) {
        String rawContent = telegram.getRawContent();
        if (ToolsKit.isEmpty(telegram) || ToolsKit.isEmpty(rawContent)) {
            LOG.info("发送的报文对象或报文内容不能为空");
            return;
        }
        LOG.info("rxtx send telegeram: {}", rawContent);
        channelFuture.channel().writeAndFlush(rawContent);
    }
}

