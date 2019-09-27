package com.openagv.plugins.tcp;

import cn.hutool.core.util.ClassUtil;
import cn.hutool.core.util.ReflectUtil;
import com.openagv.core.AppContext;
import com.openagv.core.interfaces.IEnable;
import com.openagv.core.interfaces.IPlugin;
import com.openagv.tools.SettingUtils;
import com.openagv.tools.ToolsKit;
import io.netty.channel.ChannelHandler;
import org.opentcs.contrib.tcp.netty.ConnectionEventListener;
import org.opentcs.contrib.tcp.netty.TcpClientChannelManager;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * TCP方式，基于Netty实现底层协议
 *
 * @author Laotang
 */
public class TcpPlugin implements IPlugin, IEnable {

    private Supplier<List<ChannelHandler>> channelSupplier;
    private int readTimeout;
    private boolean enableLogging;
    private TcpClientChannelManager tcpClientChannelManager;
    private ConnectionEventListener connectionEventListener;

    public TcpPlugin() {
        readTimeout = SettingUtils.getInt("tcp.read.timeout", 5000);
        enableLogging = SettingUtils.getBoolean("tcp.log.enable", false);
        String listener = SettingUtils.getString("tcp.listener");
        connectionEventListener = ReflectUtil.newInstance(listener);
        createChannelSupplier();
    }

    private void createChannelSupplier() {
        final List<ChannelHandler> channelHandlers = new ArrayList<>();
        String encodeClassString = SettingUtils.getString("tcp.encode");
        String decodeClassString = SettingUtils.getString("tcp.decode");
        if(ToolsKit.isNotEmpty(encodeClassString) &&
                ToolsKit.isNotEmpty(decodeClassString)) {
            channelHandlers.add(ReflectUtil.newInstance(encodeClassString));
            channelHandlers.add(ReflectUtil.newInstance(ClassUtil.loadClass(decodeClassString), connectionEventListener));
        }
        channelSupplier = new Supplier<List<ChannelHandler>>() {
            @Override
            public List<ChannelHandler> get() {
                return channelHandlers;
            }
        };
    }

    // 创建负责与车辆连接的渠道管理器,基于netty
    @Override
    public void start() throws Exception {
        tcpClientChannelManager = new TcpClientChannelManager<String, String>(
                connectionEventListener,
                channelSupplier,
                readTimeout,
                enableLogging);

//        AppContext.setChannelManager(tcpClientChannelManager);
    }

    @Override
    public void enable() {
        if(!tcpClientChannelManager.isInitialized()) {
            tcpClientChannelManager.initialize();
        }
    }
}
