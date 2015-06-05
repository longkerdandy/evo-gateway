package com.github.longkerdandy.evo.adapter.evo.mq;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.github.longkerdandy.evo.adapter.evo.tcp.TcpClientHandler;
import com.github.longkerdandy.evo.api.message.Message;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.JedisPubSub;

import java.io.IOException;

import static com.github.longkerdandy.evo.api.util.JsonUtils.ObjectMapper;

/**
 * Message Queue Subscriber for Evolution Adapter
 */
public class EvoSubscriberWorker extends JedisPubSub {

    // Logger
    private static final Logger logger = LoggerFactory.getLogger(EvoSubscriberWorker.class);

    private final TcpClientHandler handler;

    public EvoSubscriberWorker(TcpClientHandler handler) {
        this.handler = handler;
    }

    @Override
    public void onMessage(String channel, String message) {
        try {
            // parse json
            JavaType type = ObjectMapper.getTypeFactory().constructParametrizedType(Message.class, Message.class, JsonNode.class);
            Message<JsonNode> msg = ObjectMapper.readValue(message, type);

            logger.debug("Received message {} {} on topic {}", msg.getMsgType(), msg.getMsgId(), channel);

            // todo: validate message

            // send message
            this.handler.sendMessage(msg);
        } catch (IOException e) {
            logger.warn("Parse json message with error: {}", ExceptionUtils.getMessage(e));
        }
    }
}
