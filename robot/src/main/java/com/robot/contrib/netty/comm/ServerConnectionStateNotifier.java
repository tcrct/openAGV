/**
 * Copyright (c) The openTCS Authors.
 * <p>
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package com.robot.contrib.netty.comm;

import com.robot.contrib.netty.ConnectionEventListener;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

import static java.util.Objects.requireNonNull;

/**
 * Notifies a listener about connection state changes.
 *
 * @param <I> The type of messages handled by the ConnectionEventListener.
 * @author Stefan Walter (Fraunhofer IML)
 */
public class ServerConnectionStateNotifier<I>
        extends ChannelDuplexHandler {

    /**
     * This class' logger.
     */
    private static final Logger LOG = LoggerFactory.getLogger(ServerConnectionStateNotifier.class);

    /**
     * A map of all client entries.
     */
    private final Map<Object, ClientEntry> clientEntries;
    /**
     * The key of this notifier's connection.
     */
    private Object key;
    /**
     * The connection event listener for the established connection.
     */
    private ConnectionEventListener<I> connectionEventListener;

    /**
     * Creates a new instance.
     *
     * @param clientEntries A map of all client entries.
     */
    public ServerConnectionStateNotifier(Map<Object, ClientEntry> clientEntries) {
        this.clientEntries = requireNonNull(clientEntries, "clientEntries");
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        if (connectionEventListener != null) {
            LOG.debug("Disconnecting channel for key: '{}'.", key);
            ClientEntry entry = clientEntries.get(key);
            if (entry != null) {
                entry.setChannel(null);
            }
            connectionEventListener.onDisconnect();
        }
        ctx.fireChannelInactive();
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt)
            throws Exception {
        if (evt instanceof IdleStateEvent) {
            if (((IdleStateEvent) evt).state() == IdleState.READER_IDLE
                    && connectionEventListener != null) {
                connectionEventListener.onIdle();
            }
        } else if (evt instanceof ConnectionAssociatedEvent) {
            key = ((ConnectionAssociatedEvent) evt).getKey();
            LOG.debug("Connection associated to key: '{}'", key);
            connectionEventListener = clientEntries.get(key).getConnectionEventListener();
            if (null != connectionEventListener) {
                connectionEventListener.onConnect();
            }
        }
        super.userEventTriggered(ctx, evt);
    }
}
