package com.robot.contrib.netty.udp;

import cn.hutool.core.util.StrUtil;
import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import com.robot.mvc.main.DispatchFactory;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;
import io.netty.util.CharsetUtil;

@ChannelHandler.Sharable
public class UdpClientHandler extends SimpleChannelInboundHandler<DatagramPacket> {

    private final static Log logger = LogFactory.get();

    public UdpClientHandler(){
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket datagramPacket) throws Exception {
        try {
            // 因为Netty对UDP进行了封装，所以接收到的是DatagramPacket对象。
            String telegramData = datagramPacket.content().toString(CharsetUtil.UTF_8);
            if(StrUtil.isEmpty(telegramData)) {
                logger.error("upd client接收到的报文内容不能为空");
                return;
            }
            DispatchFactory.onIncomingTelegram(telegramData);
            // 接收到的报文，以字符串形式传递
//            ctx.channel().writeAndFlush(new DatagramPacket(Unpooled.copiedBuffer(response.toString(), CharsetUtil.UTF_8), datagramPacket.sender()));
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)throws Exception {
        ctx.close();
        logger.error("UdpClientHandler exception: " + cause.getMessage(), cause);
    }
}
