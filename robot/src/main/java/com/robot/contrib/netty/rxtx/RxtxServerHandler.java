package com.robot.contrib.netty.rxtx;

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
public class RxtxServerHandler extends SimpleChannelInboundHandler<String> {

    private final static Logger LOG = LoggerFactory.getLogger(RxtxServerHandler.class);

    private final Map<String, ClientEntry> clientEntries;

    public RxtxServerHandler(Map<String, ClientEntry> clientEntries) {
        this.clientEntries = requireNonNull(clientEntries, "clientEntries");
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String message) throws Exception {
        try {
            if (ToolsKit.isEmpty(message)) {
                LOG.error("RXTX SERVER接收到的报文内容不能为空");
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
        LOG.error("################RxtxServerHandler exception: " + cause.getMessage(), cause);
    }
}
