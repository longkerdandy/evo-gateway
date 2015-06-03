package com.github.longkerdandy.evo.api.storage;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.util.HashMap;
import java.util.Map;

/**
 * Redis Database Access Layer
 */
public class RedisStorage {

    // Redis Client Pool
    protected final JedisPool jedisPool;

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

    public String getDeviceConn(String deviceId, String field) {
        try (Jedis jedis = this.jedisPool.getResource()) {
            String key = Scheme.DEVICE_CONN(deviceId);
            return jedis.hget(key, field);
        }
    }

    public void updateDeviceConn(String deviceId, String state) {
        try (Jedis jedis = this.jedisPool.getResource()) {
            Map<String, String> conn = new HashMap<>();
            conn.put(Scheme.DEVICE_CONN_STATE, state);
            conn.put(Scheme.DEVICE_CONN_TIMESTAMP, String.valueOf(System.currentTimeMillis()));
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

    public String getDeviceAttr(String deviceId, String attrName) {
        try (Jedis jedis = this.jedisPool.getResource()) {
            String key = Scheme.DEVICE_ATTR(deviceId);
            return jedis.hget(key, attrName);
        }
    }

    public void updateDeviceAttr(String deviceId, Map<String, String> attr) {
        try (Jedis jedis = this.jedisPool.getResource()) {
            String key = Scheme.DEVICE_ATTR(deviceId);
            jedis.hmset(key, attr);
        }
    }

    public void updateDeviceAttr(String deviceId, String attrName, String attrValue) {
        try (Jedis jedis = this.jedisPool.getResource()) {
            String key = Scheme.DEVICE_ATTR(deviceId);
            jedis.hset(key, attrName, attrValue);
        }
    }
}
