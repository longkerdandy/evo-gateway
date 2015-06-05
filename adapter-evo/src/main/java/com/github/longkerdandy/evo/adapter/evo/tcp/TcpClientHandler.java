package com.github.longkerdandy.evo.adapter.evo.tcp;

import com.github.longkerdandy.evo.api.message.*;
import com.github.longkerdandy.evo.api.mq.Publisher;
import com.github.longkerdandy.evo.api.mq.Topics;
import com.github.longkerdandy.evo.api.protocol.*;
import com.github.longkerdandy.evo.api.storage.RedisStorage;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.concurrent.GenericFutureListener;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * TCP Client Handler
 */
public class TcpClientHandler extends SimpleChannelInboundHandler<Message> {

    // Logger
    private static final Logger logger = LoggerFactory.getLogger(TcpClientHandler.class);

    private final RedisStorage storage;
    private final Publisher publisher;
    private final TcpClient client;
    // Connected Devices
    private final Set<String> devices = Collections.newSetFromMap(new ConcurrentHashMap<>());
    // Channel Context
    private ChannelHandlerContext ctx;

    public TcpClientHandler(RedisStorage storage, Publisher publisher, TcpClient client) {
        this.storage = storage;
        this.publisher = publisher;
        this.client = client;
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

        // reconnect
        ctx.channel().eventLoop().schedule(() -> this.client.connect(ctx.channel().eventLoop()), 15, TimeUnit.SECONDS);

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
        logger.debug("Process ConnAck message {} from {} to {}", msg.getMsgId(), msg.getFrom(), msg.getTo());

        // mark device as connected
        if (msg.getPayload().getReturnCode() == ConnAck.RECEIVED) {
            this.devices.add(msg.getTo());

            // todo: send cached messages
        }

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
        logger.debug("Process DisconnAck message {} from {} to {}", msg.getMsgId(), msg.getFrom(), msg.getTo());

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
        logger.debug("Process TrigAck message {} from {} to {}", msg.getMsgId(), msg.getFrom(), msg.getTo());

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
            // todo: decide device type based on its descriptor
            Message<ActAck> ack = MessageFactory.newActAckMessage(ProtocolType.TCP_1_0, DeviceType.DEVICE,
                    deviceId, msg.getFrom(), msg.getMsgId(), ActAck.RECEIVED);
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
        if (this.ctx == null) {
            logger.debug("Not connected to the platform, message {} {} dropped", msg.getMsgType(), msg.getMsgId());
            return;
        }

        // if disconnect, remove from connected devices
        if (msg.getMsgType() == MessageType.DISCONNECT) {
            this.devices.remove(msg.getFrom());
        }

        // if trigger or action, check if connected
        if ((msg.getMsgType() == MessageType.TRIGGER || msg.getMsgType() == MessageType.ACTION)
                && !this.devices.contains(msg.getFrom())) {
            // send connect message
            Map<String, Object> attr = getDeviceConnectAttr(msg.getFrom());
            // todo: use correct descriptor id and override policy
            Message<Connect> conn = MessageFactory.newConnectMessage(ProtocolType.TCP_1_0, DeviceType.DEVICE,
                    msg.getFrom(), Evolution.ID, null, null, null, OverridePolicy.UPDATE, attr);
            sendMessage(conn);

            // todo: cache the message

            return;
        }

        // todo: cache the message if QoS > 0

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

    /**
     * Get device attribute for Connect message
     *
     * @param deviceId Device Id
     * @return Attribute
     */
    protected Map<String, Object> getDeviceConnectAttr(String deviceId) {
        Map<String, Object> result = new HashMap<>();
        Map<String, String> attr = this.storage.getDeviceAttr(deviceId);
        for (Map.Entry<String, String> entry : attr.entrySet()) {
            // todo: filter attributed based on descriptor
            result.put(entry.getKey(), entry.getValue());
        }
        return result;
    }
}
