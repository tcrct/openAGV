package com.robot.contrib.netty.tcp;

import com.robot.contrib.netty.ConnectionEventListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class TcpClientHandler extends SimpleChannelInboundHandler<String>  {

    private ConnectionEventListener eventListener;

    public TcpClientHandler(ConnectionEventListener listener) {
        this.eventListener = listener;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, String result) throws Exception {
        eventListener.onIncomingTelegram(result);
    }

    /**
     * 本方法用于处理异常
     * @param ctx
     * @param cause
     * @throws Exception
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        // 当出现异常就关闭连接
        cause.printStackTrace();
        ctx.close();
    }
}
