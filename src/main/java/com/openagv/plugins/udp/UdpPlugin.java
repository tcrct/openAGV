package com.openagv.plugins.udp;

import cn.hutool.core.util.ReflectUtil;
import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import com.openagv.core.AppContext;
import com.openagv.core.interfaces.IEnable;
import com.openagv.core.interfaces.IPlugin;
import com.openagv.core.interfaces.IResponse;
import com.openagv.core.interfaces.ITelegramSender;
import com.openagv.opentcs.enums.CommunicationType;
import com.openagv.tools.SettingUtils;
import com.openagv.tools.ToolsKit;
import io.netty.channel.ChannelHandler;
import org.opentcs.contrib.tcp.netty.ConnectionEventListener;
import org.opentcs.util.Assertions;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

/**
 * UDP方式，基于Netty实现底层协议
 *
 * @author Laotang
 */
public class UdpPlugin implements IPlugin, IEnable, ITelegramSender {

    private static final Log logger = LogFactory.get();

    private int port;
    private Supplier<List<ChannelHandler>> channelSupplier;
    private static int BUFFER_SIZE = 64*1024;
    private static boolean loggingInitially;
    private static UdpServerChannelManager udpServerChannelManager;
    private ConnectionEventListener eventListener;

    public UdpPlugin() {
        this(SettingUtils.getInt("port", CommunicationType.UDP.name().toLowerCase(),60000),
                SettingUtils.getBoolean("logging", CommunicationType.UDP.name().toLowerCase(),false),
                SettingUtils.getStringsToSet("broadcast", CommunicationType.UDP.name().toLowerCase())
        );
    }

    public UdpPlugin(int port, boolean logEnable, Set<String> set) {
        this.port = port;
        this.loggingInitially = logEnable;
        AppContext.setBroadcastFlag(set);
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
        eventListener = AppContext.getAgvConfigure().getConnectionEventListener();
        AppContext.setCommunicationType(CommunicationType.UDP);
    }

    @Override
    public Object enable() {
        if(!udpServerChannelManager.isInitialized()) {
            udpServerChannelManager.initialize();
            eventListener.onConnect();
            logger.info("开启车辆渠道管理器[{}]成功，监听端口:{}", "udpServerChannelManager", port);
            return udpServerChannelManager;
        }
        return null;
    }

    /**
     * 广播电报到设备
     * @param response
     */
    @Override
    public void sendTelegram(IResponse response) {
        if(null == response) {
            return;
        }
        udpServerChannelManager.send(response.toString());
    }
}
