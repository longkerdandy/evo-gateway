package com.github.longkerdandy.evo.adapter.evo.storage;

import com.github.longkerdandy.evo.api.message.Message;
import com.github.longkerdandy.evo.api.storage.RedisStorage;
import org.apache.commons.lang3.StringUtils;
import redis.clients.jedis.Jedis;

import java.io.IOException;

import static com.github.longkerdandy.evo.api.util.JsonUtils.ObjectMapper;

/**
 * Redis Database Access Layer for Evolution Adapter
 */
public class EvoRedisStorage extends RedisStorage {

    /**
     * Get cached message
     * Message has not been validated
     *
     * @param msgId Message Id
     * @return Message
     * @throws IOException Json Exception
     */
    public Message getCachedMessage(String msgId) throws IOException {
        try (Jedis jedis = this.jedisPool.getResource()) {
            String m = jedis.get(EvoScheme.CACHED_MSG(msgId));
            return StringUtils.isBlank(m) ? null : Message.parseMessage(m);
        }
    }

    /**
     * Create or Replace cached message
     * Message should be validated before invoking this method!
     *
     * @param msg Message
     * @param ttl Timeout in seconds
     * @throws IOException Json Exception
     */
    public void replaceCachedMessage(Message msg, int ttl) throws IOException {
        try (Jedis jedis = this.jedisPool.getResource()) {
            String k = EvoScheme.CACHED_MSG(msg.getMsgId());
            String m = ObjectMapper.writeValueAsString(msg);
            jedis.set(k, m);
            jedis.expire(k, ttl);
        }
    }

    /**
     * Remove cached message
     *
     * @param msgId Message Id
     */
    public void removeCachedMessage(String msgId) {
        try (Jedis jedis = this.jedisPool.getResource()) {
            jedis.del(EvoScheme.CACHED_MSG(msgId));
        }
    }
}
