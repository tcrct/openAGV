package com.openagv.plugins.udp;

import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import com.openagv.core.interfaces.IResponse;
import com.openagv.opentcs.telegrams.OrderRequest;
import com.openagv.tools.ToolsKit;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;
import io.netty.util.CharsetUtil;


public class UdpHandler extends SimpleChannelInboundHandler<DatagramPacket> {

    private final static Log logger = LogFactory.get();

    public UdpHandler(){
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket datagramPacket) throws Exception {
        try {
            // 因为Netty对UDP进行了封装，所以接收到的是DatagramPacket对象。
            String str = datagramPacket.content().toString(CharsetUtil.UTF_8);
           IResponse response = ToolsKit.sendCommand(new OrderRequest(str));
            ctx.channel().writeAndFlush(new DatagramPacket(Unpooled.copiedBuffer(response.toString(), CharsetUtil.UTF_8), datagramPacket.sender()));
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx,Throwable cause)throws Exception{
        ctx.close();
        logger.error(cause.getMessage(), cause);
    }
}
