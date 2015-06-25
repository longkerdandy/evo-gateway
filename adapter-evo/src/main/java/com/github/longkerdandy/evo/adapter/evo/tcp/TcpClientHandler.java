package com.github.longkerdandy.evo.adapter.evo.tcp;

import com.github.longkerdandy.evo.adapter.evo.storage.EvoRedisStorage;
import com.github.longkerdandy.evo.api.message.*;
import com.github.longkerdandy.evo.api.mq.Publisher;
import com.github.longkerdandy.evo.api.mq.Topics;
import com.github.longkerdandy.evo.api.protocol.DeviceType;
import com.github.longkerdandy.evo.api.protocol.MessageType;
import com.github.longkerdandy.evo.api.protocol.ProtocolType;
import com.github.longkerdandy.evo.api.protocol.QoS;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.concurrent.GenericFutureListener;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * TCP Client Handler
 */
@SuppressWarnings("unused")
public class TcpClientHandler extends SimpleChannelInboundHandler<Message> {

    // Logger
    private static final Logger logger = LoggerFactory.getLogger(TcpClientHandler.class);

    private final EvoRedisStorage storage;
    private final Publisher publisher;
    private final TcpClient client;
    // Connected Devices
    private final Set<String> cd = Collections.newSetFromMap(new ConcurrentHashMap<>());
    // Channel Context
    private ChannelHandlerContext ctx;

    public TcpClientHandler(EvoRedisStorage storage, Publisher publisher, TcpClient client) {
        this.storage = storage;
        this.publisher = publisher;
        this.client = client;
    }

    /**
     * Connected to the Evolution Platform?
     *
     * @return True if connected
     */
    public boolean isConnected() {
        return this.ctx != null;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        logger.debug("Received channel active event");

        // set context
        this.ctx = ctx;

        // pass event
        ctx.fireChannelActive();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        logger.debug("Received channel inactive event");

        // unset context
        this.ctx = null;

        // clear connected devices
        this.cd.clear();

        // reconnect
        ctx.channel().eventLoop().schedule(() -> this.client.connect(ctx.channel().eventLoop()), 15, TimeUnit.SECONDS);

        // pass event
        ctx.fireChannelInactive();
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void channelRead0(ChannelHandlerContext ctx, Message msg) throws Exception {
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
        logger.debug("Process ConnAck message {} from {} to {}", msg.getMsgId(), msg.getFrom(), msg.getTo());

        // mark device as connected
        if (msg.getPayload().getReturnCode() == ConnAck.RECEIVED) {
            this.cd.add(msg.getTo());
        }

        // acknowledge the message from cache
        this.storage.removeCachedMessage(msg.getPayload().getConnMsgId());
    }

    /**
     * Process DisconnAck Message
     * Platform -> Gateway
     *
     * @param ctx ChannelHandlerContext
     * @param msg Message<DisconnAck>
     */
    protected void onDisconnAck(ChannelHandlerContext ctx, Message<DisconnAck> msg) {
        logger.debug("Process DisconnAck message {} from {} to {}", msg.getMsgId(), msg.getFrom(), msg.getTo());

        // acknowledge the message from cache
        this.storage.removeCachedMessage(msg.getPayload().getDisconnMsgId());
    }

    /**
     * Process TrigAck Message
     * Platform -> Gateway
     *
     * @param ctx ChannelHandlerContext
     * @param msg Message<TrigAck>
     */
    protected void onTrigAck(ChannelHandlerContext ctx, Message<TrigAck> msg) {
        logger.debug("Process TrigAck message {} from {} to {}", msg.getMsgId(), msg.getFrom(), msg.getTo());

        // acknowledge the message from cache
        this.storage.removeCachedMessage(msg.getPayload().getTrigMsgId());
    }

    /**
     * Process Action Message
     * Platform -> Gateway
     *
     * @param ctx ChannelHandlerContext
     * @param msg Message<Action>
     */
    protected void onAction(ChannelHandlerContext ctx, Message<Action> msg) {
        logger.debug("Process Action message {} from {} to {}", msg.getMsgId(), msg.getFrom(), msg.getTo());

        // get mapping adapter
        String deviceId = msg.getTo();
        String adapterId = this.storage.getDeviceMapping(deviceId);
        if (StringUtils.isBlank(adapterId)) {
            logger.warn("Device {} has no mapped adapter, message dropped", deviceId);
            return;
        }

        // send back acknowledge
        if (msg.getQos() > QoS.MOST_ONCE) {
            Message<ActAck> ack = MessageFactory.newActAckMessage(
                    ProtocolType.TCP_1_0, DeviceType.DEVICE,
                    deviceId, msg.getFrom(),
                    msg.getMsgId(), ActAck.RECEIVED);
            sendMessage(ack);
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
        try {
            // if disconnect, remove from connected devices
            if (msg.getMsgType() == MessageType.DISCONNECT) {
                this.cd.remove(msg.getFrom());
            }

            // not connected to the platform
            if (!isConnected()) {
                logger.trace("Not connected to the platform, message {} {} cached", msg.getMsgType(), msg.getMsgId());
                this.storage.replaceCachedMessage(msg, 60);
                return;
            }

            // device not marked as connected
            if (msg.getMsgType() != MessageType.CONNECT && !this.cd.contains(msg.getFrom())) {
                logger.trace("Device {} not marked as connected, message {} {} cached", msg.getFrom(), msg.getMsgType(), msg.getMsgId());
                this.storage.replaceCachedMessage(msg, 60);
                return;
            }

            // send message
            sendMessage(this.ctx, msg);

            // cache message if QoS > 1
            if (msg.getQos() > QoS.MOST_ONCE) {
                this.storage.replaceCachedMessage(msg, 60);
            }
        } catch (IOException e) {
            logger.error("Exception when trying to send message: {}", ExceptionUtils.getMessage(e));
        }
    }

    /**
     * Send message with specific ChannelHandlerContext
     */
    protected void sendMessage(ChannelHandlerContext ctx, Message msg) {
        ChannelFuture future = ctx.writeAndFlush(msg);
        future.addListener(new GenericFutureListener<ChannelFuture>() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                if (future.isSuccess()) {
                    logger.debug("Message {} {} has been sent from {} to {} successfully",
                            msg.getMsgType(),
                            msg.getMsgId(),
                            msg.getFrom(),
                            msg.getTo());
                } else {
                    logger.debug("Message {} {} failed to send from {} to {}: {}",
                            msg.getMsgType(),
                            msg.getMsgId(),
                            msg.getFrom(),
                            msg.getTo(),
                            ExceptionUtils.getMessage(future.cause()));
                }
            }
        });
    }
}
