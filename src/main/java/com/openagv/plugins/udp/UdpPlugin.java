package com.openagv.plugins.udp;

import cn.hutool.core.util.ReflectUtil;
import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import com.openagv.core.AppContext;
import com.openagv.core.interfaces.IEnable;
import com.openagv.core.interfaces.IPlugin;
import com.openagv.tools.SettingUtils;
import com.openagv.tools.ToolsKit;
import io.netty.channel.ChannelHandler;
import org.opentcs.util.Assertions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

/**
 * UDP方式，基于Netty实现底层协议
 *
 * @author Laotang
 */
public class UdpPlugin implements IPlugin, IEnable {

    private static final Log logger = LogFactory.get();

    private int port;
    private Supplier<List<ChannelHandler>> channelSupplier;
    private static int BUFFER_SIZE = 64*1024;
    private static boolean loggingInitially;
    private static UdpServerChannelManager udpServerChannelManager;

    private UdpPlugin() {
        this.port = SettingUtils.getInt("upd.port", 60000);
        this.loggingInitially = SettingUtils.getBoolean("upd.logging", false);
        createChannelSupplier();
    }

    private void createChannelSupplier() {
        final List<ChannelHandler> channelHandlers = new ArrayList<>();
        String encodeClassString = SettingUtils.getString("upd.encode.class");
        String decodeClassString = SettingUtils.getString("upd.decode.class");
        if(ToolsKit.isNotEmpty(encodeClassString) && ToolsKit.isNotEmpty(decodeClassString)) {
            channelHandlers.add(ReflectUtil.newInstance(encodeClassString));
            channelHandlers.add(ReflectUtil.newInstance(decodeClassString));
        }
        channelSupplier = new Supplier<List<ChannelHandler>>() {
            @Override
            public List<ChannelHandler> get() {
                return channelHandlers;
            }
        };
    }

    @Override
    public void start() throws Exception {
        Assertions.checkArgument(port > 0, "port <= 0: %s", new Object[]{port});
        java.util.Objects.requireNonNull(channelSupplier, "channelSupplier");
        udpServerChannelManager = new UdpServerChannelManager(port, channelSupplier, loggingInitially, BUFFER_SIZE);
    }

    @Override
    public void enable() {
        if(!udpServerChannelManager.isInitialized()) {
            udpServerChannelManager.initialize();
            logger.info("开启车辆渠道管理器[{}]成功!", "udpServerChannelManager");
        }
    }
}
