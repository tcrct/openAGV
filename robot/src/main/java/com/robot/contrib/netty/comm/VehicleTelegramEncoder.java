package com.robot.contrib.netty.comm;

import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.string.StringEncoder;

import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.util.List;

/**
 * 车辆电报(字符串)编码
 *
 * @author Laotang
 */
public class VehicleTelegramEncoder extends StringEncoder {

    protected void encode(ChannelHandlerContext ctx, CharSequence msg, List<Object> out) throws Exception {

        System.out.println("#############VehicleTelegramEncoder:   " + msg);

        if (msg.length() != 0) {
            out.add(ByteBufUtil.encodeString(ctx.alloc(), CharBuffer.wrap(msg), Charset.defaultCharset()));
        }
    }
}
