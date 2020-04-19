package com.robot.contrib.netty.rxtx;

import com.robot.contrib.netty.ConnectionEventListener;
import com.robot.contrib.netty.comm.ClientEntry;
import com.robot.utils.RobotUtil;
import com.robot.utils.ToolsKit;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

import static java.util.Objects.requireNonNull;

/**
 * 串口消息处理器
 * Created by laotang on 2020/1/20.
 */
public class RxtxClientHandler extends SimpleChannelInboundHandler<String> {

    private final static Logger LOG = LoggerFactory.getLogger(RxtxClientHandler.class);

    private ConnectionEventListener eventListener;

    public RxtxClientHandler(ConnectionEventListener listener) {
        this.eventListener = listener;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String message) throws Exception {
        try {
            if (ToolsKit.isEmpty(message)) {
                LOG.error("RXTX CLIENT接收到的报文内容不能为空");
                return;
            }
            // 将接收到的报文转至事件监听器进行处理
            eventListener.onIncomingTelegram(message);
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }

    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        LOG.error("################RxtxClientHandler channelUnregistered: " + ctx.channel().id());
        ctx.fireChannelUnregistered();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        LOG.error("################RxtxClientHandler exception: " + cause.getMessage(), cause);
    }
}
