package com.robot.contrib.netty.udp;

import com.robot.contrib.netty.comm.ClientEntry;
import com.robot.utils.RobotUtil;
import com.robot.utils.ToolsKit;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;
import io.netty.util.CharsetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

import static java.util.Objects.requireNonNull;

@ChannelHandler.Sharable
public class UdpServerHandler extends SimpleChannelInboundHandler<DatagramPacket> {

    private final static Logger LOG = LoggerFactory.getLogger(UdpServerHandler.class);
    private final Map<String, ClientEntry> clientEntries;

    public UdpServerHandler(Map<String, ClientEntry> clientEntries) {
        this.clientEntries = requireNonNull(clientEntries, "clientEntries");
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket datagramPacket) throws Exception {
        try {
            // 因为Netty对UDP进行了封装，所以接收到的是DatagramPacket对象。
            String message = datagramPacket.content().toString(CharsetUtil.UTF_8);
            if (ToolsKit.isEmpty(message)) {
                LOG.error("UPD SERVER接收到的报文内容不能为空");
                return;
            }
            // 将接收到的报文转至调度工厂进行处理
            RobotUtil.channelReadToDispatchFactory(ctx.channel(), clientEntries, message);
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        LOG.error("################UdpServerHandler exception: " + cause.getMessage(), cause);
    }
}
