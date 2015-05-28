package com.github.longkerdandy.evo.api.mq;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.longkerdandy.evo.api.message.Message;
import com.github.longkerdandy.evo.api.util.JsonUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

/**
 * Message Queue Publisher
 */
@SuppressWarnings("unused")
public class Publisher {

    // Logger
    private static final Logger logger = LoggerFactory.getLogger(Publisher.class);

    // Redis Client Pool
    private final JedisPool jedisPool;

    public Publisher() {
        this("localhost", 6379);
    }

    public Publisher(String host, int port) {
        this.jedisPool = new JedisPool(new JedisPoolConfig(), host, port);
    }

    /**
     * Close Publisher
     */
    public void close() {
        if (this.jedisPool != null) this.jedisPool.destroy();
    }

    /**
     * Send message to message queue
     *
     * @param topic Message Queue Topic
     * @param msg   Message
     */
    public void sendMessage(String topic, Message msg) {
        try (Jedis jedis = jedisPool.getResource()) {
            String value = JsonUtils.ObjectMapper.writeValueAsString(msg);
            long count = jedis.publish(topic, value);
            logger.debug("Successful send message {} {} to mq {} subscriber", msg.getMsgType(), msg.getMsgId(), count);
        } catch (JsonProcessingException e) {
            logger.error("Send message {} {} to mq with exception: {}", msg.getMsgType(), msg.getMsgId(), ExceptionUtils.getMessage(e));
        }
    }
}
