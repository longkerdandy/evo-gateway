package com.github.longkerdandy.evo.adapter.evo.mq;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.github.longkerdandy.evo.adapter.evo.tcp.TcpClient;
import com.github.longkerdandy.evo.api.message.*;
import com.github.longkerdandy.evo.api.protocol.MessageType;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.JedisPubSub;

import java.io.IOException;

import static com.github.longkerdandy.evo.api.util.JsonUtils.ObjectMapper;

/**
 * Message Queue Subscriber Worker for Evolution Adapter
 */
public class EvoSubscriberWorker extends JedisPubSub {

    // Logger
    private static final Logger logger = LoggerFactory.getLogger(EvoSubscriberWorker.class);

    private final TcpClient client;

    public EvoSubscriberWorker(TcpClient client) {
        this.client = client;
    }

    @Override
    public void onMessage(String channel, String message) {
        try {
            // two step parse json
            JavaType type = ObjectMapper.getTypeFactory().constructParametrizedType(Message.class, Message.class, JsonNode.class);
            Message<JsonNode> m = ObjectMapper.readValue(message, type);
            if (m.getTimestamp() <= 0) m.setTimestamp(System.currentTimeMillis());  // if timestamp not provided
            Message msg;
            switch (m.getMsgType()) {
                case MessageType.CONNECT:
                    msg = MessageFactory.newMessage(m, ObjectMapper.treeToValue(m.getPayload(), Connect.class));
                    break;
                case MessageType.CONNACK:
                    msg = MessageFactory.newMessage(m, ObjectMapper.treeToValue(m.getPayload(), ConnAck.class));
                    break;
                case MessageType.DISCONNECT:
                    msg = MessageFactory.newMessage(m, ObjectMapper.treeToValue(m.getPayload(), Disconnect.class));
                    break;
                case MessageType.DISCONNACK:
                    msg = MessageFactory.newMessage(m, ObjectMapper.treeToValue(m.getPayload(), DisconnAck.class));
                    break;
                case MessageType.TRIGGER:
                    msg = MessageFactory.newMessage(m, ObjectMapper.treeToValue(m.getPayload(), Trigger.class));
                    break;
                case MessageType.TRIGACK:
                    msg = MessageFactory.newMessage(m, ObjectMapper.treeToValue(m.getPayload(), TrigAck.class));
                    break;
                case MessageType.ACTION:
                    msg = MessageFactory.newMessage(m, ObjectMapper.treeToValue(m.getPayload(), Action.class));
                    break;
                case MessageType.ACTACK:
                    msg = MessageFactory.newMessage(m, ObjectMapper.treeToValue(m.getPayload(), ActAck.class));
                    break;
                default:
                    logger.warn("Unexpected message type: " + m.getMsgType());
                    return;
            }

            logger.debug("Received message {} {} on topic {}", msg.getMsgType(), msg.getMsgId(), channel);

            // validate
            msg.validate();
            // send message
            this.client.getHandler().sendMessage(msg);
        } catch (IOException | IllegalStateException e) {
            logger.warn("Parse message with error: {}", ExceptionUtils.getMessage(e));
        }
    }
}
