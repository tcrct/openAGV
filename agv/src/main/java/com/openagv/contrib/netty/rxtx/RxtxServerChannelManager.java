package com.openagv.contrib.netty.rxtx;

import cn.hutool.core.util.ObjectUtil;
import com.openagv.contrib.netty.comm.IChannelManager;
import com.openagv.contrib.netty.udp.UdpClientChannelManager;
import com.openagv.mvc.core.interfaces.IRequest;
import com.openagv.mvc.core.interfaces.IResponse;
import com.openagv.mvc.utils.ToolsKit;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.oio.OioEventLoopGroup;
import io.netty.channel.rxtx.RxtxChannel;
import io.netty.channel.rxtx.RxtxDeviceAddress;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.util.CharsetUtil;
import org.opentcs.contrib.tcp.netty.ConnectionEventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.util.Iterator;
import java.util.List;
import java.util.function.Supplier;

/**
 * Created by laotang on 2020/1/20.
 */
public class RxtxServerChannelManager implements IChannelManager<IRequest, IResponse> {

    private static final Logger LOG = LoggerFactory.getLogger(RxtxServerChannelManager.class);

    private ConnectionEventListener<IRequest> eventListener;
    private Bootstrap bootstrap;
    private OioEventLoopGroup workerGroup;
    private ChannelFuture channelFuture;
    private RxtxChannel rxtxChannel;
    private int readTimeout;
    private boolean enableLogging;
    /**是否已经初始化*/
    private boolean initialized;
    private boolean connected;

    private Supplier<List<ChannelHandler>> channelSupplier;

    public RxtxServerChannelManager(Supplier<List<ChannelHandler>> channelSupplier,
                                    int readTimeout,
                                    boolean enableLogging) {
        this.channelSupplier = channelSupplier;
        this.readTimeout = readTimeout;
        this.enableLogging = enableLogging;
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
                        }
                    });
            initialized = true;
        } catch (Exception e) {
            workerGroup.shutdownGracefully();
        }
    }

    public boolean isInitialized() {
        return initialized;
    }

    public void terminate() {
        if (isInitialized() && isConnected()) {
            workerGroup.shutdownGracefully();
            this.channelFuture.channel().close();
            this.initialized = false;
            this.channelFuture = null;
            bootstrap = null;
            LOG.warn("RxtxServerChannelManager is disconnect!");
        }
    }

    @Override
    public void connect(String serialport, int baudrate) {
        if (!isInitialized()) {
            throw new RuntimeException("指定的串口初始化不成功");
        }
        if (baudrate == 0) {
            throw new IllegalArgumentException("串口波特率[" + baudrate + "]没有设置");
        }
        try {
            rxtxChannel = new RxtxChannel();
            rxtxChannel.config().setBaudrate(baudrate);
            channelFuture = bootstrap.connect(new RxtxDeviceAddress(serialport)).sync();
            channelFuture.addListener((ChannelFuture future) -> {
                if (future.isSuccess()) {
                    this.connected = true;
                    LOG.warn("rxtx链接成功:  {}:{}", serialport, baudrate);
                }
                else {
                    throw new InterruptedException("rxtx链接失败");
                }
            });
            eventListener.onConnect();
            LOG.info("串口连接并监听成功，名称[{}]，波特率[{}]", serialport, baudrate);
        } catch (Exception e) {
            workerGroup.shutdownGracefully();
            throw new RuntimeException("打开串口时失败，名称[" + serialport + "]， 波特率[" + baudrate + "], 串口可能已被占用！");
        }
    }

    @Override
    public void disconnect() {
        terminate();
    }

    @Override
    public boolean isConnected() {
        return connected;
    }

    @Override
    public void setLoggingEnabled(boolean enable) {
        LOG.info("该功能未实现");
    }

    @Override
    public void scheduleConnect(@Nonnull String host, int port, long delay) {
        LOG.info("该功能未实现");
    }

    @Override
    public void send(IResponse telegram) {
        String rawContent = telegram.getRawContent();
        if (ToolsKit.isEmpty(telegram) || ToolsKit.isEmpty(rawContent)) {
            LOG.info("发送的报文对象或报文内容不能为空");
            return;
        }
        LOG.info("send telegeram: {}", rawContent);
        channelFuture.channel().writeAndFlush(rawContent);
    }
}

