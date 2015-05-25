package com.github.longkerdandy.evo.api.storage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.util.Map;

/**
 * Redis Database Access Layer
 */
public class RedisStorage {

    // Logger
    private static final Logger logger = LoggerFactory.getLogger(RedisStorage.class);

    // Redis Client Pool
    private final JedisPool jedisPool;

    public RedisStorage() {
        this("localhost", 6379);
    }

    public RedisStorage(String host, int port) {
        this.jedisPool = new JedisPool(new JedisPoolConfig(), host, port);
    }

    /**
     * Close Publisher
     */
    public void close() {
        if (this.jedisPool != null) this.jedisPool.destroy();
    }

    public Map<String, String> getAdapter(String adapterId) {
        try (Jedis jedis = this.jedisPool.getResource()) {
            String key = Scheme.ADAPTER(adapterId);
            return jedis.hgetAll(key);
        }
    }

    public void updateAdapter(String adapterId, Map<String, String> attr) {
        try (Jedis jedis = this.jedisPool.getResource()) {
            String key = Scheme.ADAPTER(adapterId);
            jedis.hmset(key, attr);
        }
    }

    public void updateDeviceMapping(String deviceId, String adapterId) {
        try (Jedis jedis = this.jedisPool.getResource()) {
            String key = Scheme.DEVICE_MAPPING;
            jedis.hset(key, deviceId, adapterId);
        }
    }

    public Map<String, String> getDeviceConn(String deviceId) {
        try (Jedis jedis = this.jedisPool.getResource()) {
            String key = Scheme.DEVICE_CONN(deviceId);
            return jedis.hgetAll(key);
        }
    }

    public void updateDeviceConn(String deviceId, Map<String, String> conn) {
        try (Jedis jedis = this.jedisPool.getResource()) {
            String key = Scheme.DEVICE_CONN(deviceId);
            jedis.hmset(key, conn);
        }
    }

    public Map<String, String> getDeviceAttr(String deviceId) {
        try (Jedis jedis = this.jedisPool.getResource()) {
            String key = Scheme.DEVICE_ATTR(deviceId);
            return jedis.hgetAll(key);
        }
    }

    public void updateDeviceAttr(String deviceId, Map<String, String> attr) {
        try (Jedis jedis = this.jedisPool.getResource()) {
            String key = Scheme.DEVICE_ATTR(deviceId);
            jedis.hmset(key, attr);
        }
    }
}
