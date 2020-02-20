package com.robot.contrib.netty.tcp;

import com.robot.contrib.netty.comm.ClientEntry;
import com.robot.contrib.netty.comm.ConnectionAssociatedEvent;
import com.robot.utils.RobotUtil;
import com.robot.utils.ToolsKit;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Iterator;
import java.util.Map;

import static java.util.Objects.requireNonNull;

@ChannelHandler.Sharable
public class TcpServerHandler extends SimpleChannelInboundHandler<String> {

    private final static Logger LOG = LoggerFactory.getLogger(TcpServerHandler.class);
    private final Map<String, ClientEntry> clientEntries;
    private Object objKey;

    public TcpServerHandler(Map<String, ClientEntry> clientEntries) {
        this.clientEntries = requireNonNull(clientEntries, "clientEntries");
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        InetSocketAddress remoteAddress = (InetSocketAddress)ctx.channel().remoteAddress();
        String host = remoteAddress.getAddress().getHostAddress();
        ClientEntry clientEntry = getClientEntry(host);
        if (null == clientEntry) {
            clientEntry = new ClientEntry(host, host, 0,null);
        }
        clientEntry.setChannel(ctx.channel());
        clientEntries.put(clientEntry.getKey(), clientEntry);
        LOG.info("将host[{}]的Channel[{}]缓存到ClientEntries，待注册设置Channel后移除", host, clientEntry.getChannel().id());
        objKey = clientEntry.getKey();
        ctx.fireChannelActive();
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        // 调用ServerConnectionStateNotifier里的userEventTriggered方法
        ctx.fireUserEventTriggered(new ConnectionAssociatedEvent(objKey));
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String message) throws Exception {
        try {
            if (ToolsKit.isEmpty(message)) {
                LOG.error("TCP SERVER接收到的报文内容不能为空");
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
//        InetSocketAddress remoteAddress = (InetSocketAddress)ctx.channel().remoteAddress();
//        String host = remoteAddress.getAddress().getHostAddress();
//        ClientEntry clientEntry = getClientEntry(host);
//        clientEntries.remove(clientEntry.getKey());
//        LOG.info("远程主机强迫关闭了一个现有的连接时，移除Map[clientEntries]里Key为[{}]的ClientEntry对象", clientEntry.getKey());
        LOG.error("################TcpServerHandler exception: " + cause.getMessage(), cause);
    }

    private ClientEntry getClientEntry(String host) {
        ClientEntry clientEntry = null;
        for (Iterator<Map.Entry<String, ClientEntry>> iterator = clientEntries.entrySet().iterator(); iterator.hasNext(); ) {
            Map.Entry<String, ClientEntry> entry = iterator.next();
            ClientEntry subClientEntry = entry.getValue();
            if (host.equals(subClientEntry.getHost())) {
                clientEntry = subClientEntry;
                break;
            }
        }
        return clientEntry;
    }
}
