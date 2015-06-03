package com.github.longkerdandy.evo.adapter.evo.tcp;

import com.github.longkerdandy.evo.api.message.Message;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * TCP Client Handler
 */
public class TcpClientHandler extends SimpleChannelInboundHandler<Message> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Message msg) throws Exception {

    }
}
