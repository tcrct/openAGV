package com.robot.contrib.netty.udp;

import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import com.robot.contrib.netty.comm.ClientEntry;
import com.robot.mvc.core.interfaces.IProtocol;
import com.robot.mvc.main.DispatchFactory;
import com.robot.mvc.utils.RobotUtil;
import com.robot.mvc.utils.ToolsKit;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;
import io.netty.util.CharsetUtil;

import java.util.List;
import java.util.Map;

import static java.util.Objects.requireNonNull;

@ChannelHandler.Sharable
public class UdpServerHandler extends SimpleChannelInboundHandler<DatagramPacket> {

    private final static Log LOG = LogFactory.get();
    private final Map<String, ClientEntry> clientEntries;

    public UdpServerHandler(Map<String, ClientEntry> clientEntries) {
        this.clientEntries = requireNonNull(clientEntries, "clientEntries");
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket datagramPacket) throws Exception {
        try {
            // 因为Netty对UDP进行了封装，所以接收到的是DatagramPacket对象。
            String telegramData = datagramPacket.content().toString(CharsetUtil.UTF_8);
            if (ToolsKit.isEmpty(telegramData)) {
                LOG.error("upd client接收到的报文内容不能为空");
                return;
            }
            // 设置通讯通道到客户端对象
            List<IProtocol> protocolList = RobotUtil.toProtocolList(telegramData);
            if (ToolsKit.isEmpty(protocolList)) {
                LOG.warn("将接收到的报文[{}]转换为List<IProtocol>时，List对象为空！", telegramData);
                return;
            }
            for (IProtocol protocol : protocolList) {
                String key = protocol.getDeviceId();
                if (ToolsKit.isEmpty(key)) {
                    LOG.warn("车辆/设备标识符不能为空");
                    continue;
                }
                ClientEntry client = clientEntries.get(key);
                if (ToolsKit.isEmpty(client)) {
                    LOG.warn("根据车辆/设备标识符[{}]查找不到对应的ClientEntry对象，退出本次访问，请检查！");
                    continue;
                }
                client.setChannel(ctx.channel());
                DispatchFactory.onIncomingTelegram(protocol);

            }
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
//        ctx.close();
        LOG.error("UdpServerHandler exception: " + cause.getMessage(), cause);
    }
}
