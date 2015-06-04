package com.github.longkerdandy.evo.adapter.evo.tcp;

import com.github.longkerdandy.evo.api.message.*;
import com.github.longkerdandy.evo.api.mq.Publisher;
import com.github.longkerdandy.evo.api.mq.Topics;
import com.github.longkerdandy.evo.api.protocol.MessageType;
import com.github.longkerdandy.evo.api.storage.RedisStorage;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.concurrent.GenericFutureListener;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TCP Client Handler
 */
public class TcpClientHandler extends SimpleChannelInboundHandler<Message> {

    // Logger
    private static final Logger logger = LoggerFactory.getLogger(TcpClientHandler.class);

    private final RedisStorage storage;
    private final Publisher publisher;
    private ChannelHandlerContext ctx;

    public TcpClientHandler(RedisStorage storage, Publisher publisher) {
        this.storage = storage;
        this.publisher = publisher;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        // set context
        this.ctx = ctx;
        // pass event
        ctx.fireChannelActive();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        // unset context
        this.ctx = null;
        // pass event
        ctx.fireChannelInactive();
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void channelRead0(ChannelHandlerContext ctx, Message msg) throws Exception {
        // todo: validate message

        switch (msg.getMsgType()) {
            case MessageType.CONNACK:
                onConnAck(ctx, (Message<ConnAck>) msg);
                break;
            case MessageType.DISCONNACK:
                onDisconnAck(ctx, (Message<DisconnAck>) msg);
                break;
            case MessageType.TRIGACK:
                onTrigAck(ctx, (Message<TrigAck>) msg);
                break;
            case MessageType.ACTION:
                onAction(ctx, (Message<Action>) msg);
                break;
        }
    }

    /**
     * Process ConnAck Message
     * Platform -> Gateway
     *
     * @param ctx ChannelHandlerContext
     * @param msg Message<ConnAck>
     */
    protected void onConnAck(ChannelHandlerContext ctx, Message<ConnAck> msg) {
        logger.debug("Process ConnAck message {} from device {}", msg.getMsgId(), msg.getFrom());
        // todo: acknowledge the message from cache
    }

    /**
     * Process DisconnAck Message
     * Platform -> Gateway
     *
     * @param ctx ChannelHandlerContext
     * @param msg Message<DisconnAck>
     */
    protected void onDisconnAck(ChannelHandlerContext ctx, Message<DisconnAck> msg) {
        logger.debug("Process DisconnAck message {} from device {}", msg.getMsgId(), msg.getFrom());
        // todo: acknowledge the message from cache
    }

    /**
     * Process TrigAck Message
     * Platform -> Gateway
     *
     * @param ctx ChannelHandlerContext
     * @param msg Message<TrigAck>
     */
    protected void onTrigAck(ChannelHandlerContext ctx, Message<TrigAck> msg) {
        logger.debug("Process TrigAck message {} from device {}", msg.getMsgId(), msg.getFrom());
        // todo: acknowledge the message from cache
    }

    /**
     * Process Action Message
     * Platform -> Gateway
     *
     * @param ctx ChannelHandlerContext
     * @param msg Message<Action>
     */
    protected void onAction(ChannelHandlerContext ctx, Message<Action> msg) {
        logger.debug("Process Action message {} from device {}", msg.getMsgId(), msg.getFrom());

        // mapping adapter
        String deviceId = msg.getTo();
        String adapterId = this.storage.getDeviceMapping(deviceId);
        if (StringUtils.isBlank(adapterId)) {
            logger.warn("Device {} has no mapped adapter, message dropped", deviceId);
            return;
        }

        // push to mq
        this.publisher.sendMessage(Topics.DEVICE_ADAPTER(adapterId), msg);
    }

    /**
     * Send message to platform
     *
     * @param msg Message to be sent
     */
    public void sendMessage(Message msg) {
        if (this.ctx == null) {
            logger.debug("Not connected to the platform, message {} {} dropped", msg.getMsgType(), msg.getMsgId());
            return;
        }

        // todo: cache the message

        ChannelFuture future = this.ctx.writeAndFlush(msg);
        future.addListener(new GenericFutureListener<ChannelFuture>() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                if (future.isSuccess()) {
                    logger.debug("Message {} {} has been sent to device {} successfully",
                            msg.getMsgType(),
                            msg.getMsgId(),
                            StringUtils.defaultIfBlank(msg.getTo(), "<null>"));
                } else {
                    logger.debug("Message {} {} failed to send to device {}: {}",
                            msg.getMsgType(),
                            msg.getMsgId(),
                            StringUtils.defaultIfBlank(msg.getTo(), "<null>"),
                            ExceptionUtils.getMessage(future.cause()));
                }
            }
        });
    }
}
