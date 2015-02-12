package com.github.longkerdandy.evo.gateway.handler;

import com.github.longkerdandy.evo.gateway.repo.ChannelRepository;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.mqtt.MqttMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Business Handler
 */
public class BusinessHandler extends SimpleChannelInboundHandler<MqttMessage> {

    // Logger
    private static final Logger logger = LoggerFactory.getLogger(BusinessHandler.class);

    private final ChannelRepository channelRepository;         // Connection Repository

    public BusinessHandler(ChannelRepository channelRepository) {
        this.channelRepository = channelRepository;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, MqttMessage msg) throws Exception {

    }
}
