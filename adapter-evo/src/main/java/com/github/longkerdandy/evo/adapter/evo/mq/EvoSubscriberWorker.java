package com.github.longkerdandy.evo.adapter.evo.mq;

import com.github.longkerdandy.evo.adapter.evo.tcp.TcpClient;
import com.github.longkerdandy.evo.api.message.Message;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.JedisPubSub;

import java.io.IOException;

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
            // parse json
            Message msg = Message.parseMessage(message);
            if (msg.getTimestamp() <= 0) msg.setTimestamp(System.currentTimeMillis());  // if timestamp not provided

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
