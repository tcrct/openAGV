/**
 * Copyright (c) Fraunhofer IML
 */
package com.robot.contrib.netty.comm;

import com.robot.utils.RobotUtil;
import com.robot.utils.ToolsKit;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.socket.DatagramPacket;
import io.netty.util.CharsetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

import static java.util.Objects.requireNonNull;

/**
 * 将传入消息与感兴趣的客户端关联。
 * 这里总是只有一个客户端。
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
@Deprecated
public class ConnectionAssociator
        extends ChannelInboundHandlerAdapter {

    /**
     * This class's Logger.
     */
    private static final Logger LOG = LoggerFactory.getLogger(ConnectionAssociator.class);
    /**
     * A pool of clients that may connect to a TcpServerChannelManager.
     */
    private final Map<Object, ClientEntry> clientEntries;
    /**
     * The associated client.
     */
    private ClientEntry client;

    public ConnectionAssociator(Map<Object, ClientEntry> clientEntries) {
        this.clientEntries = requireNonNull(clientEntries, "clientEntries");
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        handleMessage(ctx, msg);
    }

    private void handleMessage(ChannelHandlerContext ctx, Object msg) {
        String telegramData = "";
        if (NetChannelType.UDP.equals(RobotUtil.getNetChannelType()) && (msg instanceof DatagramPacket)) {
            telegramData = ((DatagramPacket) msg).content().toString(CharsetUtil.UTF_8);
        } else if ((NetChannelType.TCP.equals(RobotUtil.getNetChannelType()) || NetChannelType.RXTX.equals(RobotUtil.getNetChannelType())) &&
                (msg instanceof String)) {
            telegramData = String.valueOf(msg);
            return;
        }

        if (ToolsKit.isEmpty(telegramData)) {
            LOG.error("接收的报文内容不能为空");
            return;
        }

        String key = RobotUtil.getCleintEntryKey(telegramData);

        if (client == null) {
            LOG.debug("Received the first data from the client.");
            client = clientEntries.get(key);

            // If no one is interested ignore the message and close the connection.
            if (client == null) {
                LOG.info("Ignoring message for unknown key '{}'. Registered entries: {}", key, clientEntries.keySet());
//                ctx.close();
                return;
            }
            // 设置通讯通道到客户端对象
            client.setChannel(ctx.channel());
            // Notify any listeners that the channel has been associated to this nickname, implicitly
            // notifying the comm adapter that a connection has been established.
            ctx.fireUserEventTriggered(new ConnectionAssociatedEvent(key));
        }
    }
}
