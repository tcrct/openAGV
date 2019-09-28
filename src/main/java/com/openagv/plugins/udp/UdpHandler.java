package com.openagv.plugins.udp;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import com.openagv.core.AgvResult;
import com.openagv.core.AppContext;
import com.openagv.core.Main;
import com.openagv.core.command.SendCommand;
import com.openagv.core.interfaces.IResponse;
import com.openagv.core.interfaces.ITelegram;
import com.openagv.exceptions.AgvException;
import com.openagv.opentcs.model.Telegram;
import com.openagv.tools.SettingUtils;
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
           AgvResult result = ToolsKit.sendCommand(new Telegram(str));
            ctx.channel().writeAndFlush(new DatagramPacket(Unpooled.copiedBuffer(result.getResponse().toString(), CharsetUtil.UTF_8), datagramPacket.sender()));
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
