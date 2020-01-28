package com.robot.contrib.netty.comm;

import com.robot.mvc.main.DispatchFactory;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.util.CharsetUtil;

import java.util.List;

/**
 * 车辆电报(字符串)解码
 *
 * @author Laotang
 */
public class VehicleTelegramDecoder extends StringDecoder {

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out) throws Exception {
        String telegramData = msg.toString(CharsetUtil.UTF_8);
        // 接收到的协议，即车辆或设备提交的协议字符串内容
        DispatchFactory.onIncomingTelegram(telegramData);
    }
}
