package com.github.longkerdandy.evo.api.storage;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.util.HashMap;
import java.util.Map;

/**
 * Redis Database Access Layer
 */
@SuppressWarnings("unused")
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

    /**
     * Get adapter information
     *
     * @param adapterId Adapter Id
     * @return Adapter Information
     */
    public Map<String, String> getAdapter(String adapterId) {
        try (Jedis jedis = this.jedisPool.getResource()) {
            String key = Scheme.ADAPTER(adapterId);
            return jedis.hgetAll(key);
        }
    }

    /**
     * Update adapter information
     *
     * @param adapterId Adapter Id
     * @param info      Information
     */
    public void updateAdapter(String adapterId, Map<String, String> info) {
        try (Jedis jedis = this.jedisPool.getResource()) {
            String key = Scheme.ADAPTER(adapterId);
            jedis.hmset(key, info);
        }
    }

    /**
     * Update device id - adapter id mapping
     * We must keep tracking which adapter handle which device
     *
     * @param deviceId  Device Id
     * @param adapterId Adapter Id
     */
    public void updateDeviceMapping(String deviceId, String adapterId) {
        try (Jedis jedis = this.jedisPool.getResource()) {
            String key = Scheme.DEVICE_MAPPING;
            jedis.hset(key, deviceId, adapterId);
        }
    }

    /**
     * Get device mapped adapter id
     *
     * @param deviceId Device Id
     * @return Adapter Id
     */
    public String getDeviceMapping(String deviceId) {
        try (Jedis jedis = this.jedisPool.getResource()) {
            String key = Scheme.DEVICE_MAPPING;
            return jedis.hget(key, deviceId);
        }
    }

    /**
     * Get device connection information
     *
     * @param deviceId Device Id
     * @return Connection Information
     */
    public Map<String, String> getDeviceConn(String deviceId) {
        try (Jedis jedis = this.jedisPool.getResource()) {
            String key = Scheme.DEVICE_CONN(deviceId);
            return jedis.hgetAll(key);
        }
    }

    /**
     * Get device connection specific information
     *
     * @param deviceId Device Id
     * @param field    Information field
     * @return Connection Information
     */
    public String getDeviceConn(String deviceId, String field) {
        try (Jedis jedis = this.jedisPool.getResource()) {
            String key = Scheme.DEVICE_CONN(deviceId);
            return jedis.hget(key, field);
        }
    }

    /**
     * Update device connection information
     *
     * @param deviceId Device Id
     * @param state    Connection state, 0 means disconnected. 1 means connected
     */
    public void updateDeviceConn(String deviceId, String state) {
        try (Jedis jedis = this.jedisPool.getResource()) {
            Map<String, String> conn = new HashMap<>();
            conn.put(Scheme.DEVICE_CONN_STATE, state);
            conn.put(Scheme.DEVICE_CONN_TIMESTAMP, String.valueOf(System.currentTimeMillis()));
            String key = Scheme.DEVICE_CONN(deviceId);
            jedis.hmset(key, conn);
        }
    }

    /**
     * Get device attribute
     *
     * @param deviceId Device Id
     * @return Attribute
     */
    public Map<String, String> getDeviceAttr(String deviceId) {
        try (Jedis jedis = this.jedisPool.getResource()) {
            String key = Scheme.DEVICE_ATTR(deviceId);
            return jedis.hgetAll(key);
        }
    }

    /**
     * Get device specific attribute
     *
     * @param deviceId Device Id
     * @param attrName Attribute Name
     * @return Attribute Value
     */
    public String getDeviceAttr(String deviceId, String attrName) {
        try (Jedis jedis = this.jedisPool.getResource()) {
            String key = Scheme.DEVICE_ATTR(deviceId);
            return jedis.hget(key, attrName);
        }
    }

    /**
     * Update device attribute
     *
     * @param deviceId Device Id
     * @param attr     Attribute
     */
    public void updateDeviceAttr(String deviceId, Map<String, String> attr) {
        try (Jedis jedis = this.jedisPool.getResource()) {
            String key = Scheme.DEVICE_ATTR(deviceId);
            jedis.hmset(key, attr);
        }
    }

    /**
     * Update device specific attribute
     *
     * @param deviceId  Device Id
     * @param attrName  Attribute Name
     * @param attrValue Attribute Value
     */
    public void updateDeviceAttr(String deviceId, String attrName, String attrValue) {
        try (Jedis jedis = this.jedisPool.getResource()) {
            String key = Scheme.DEVICE_ATTR(deviceId);
            jedis.hset(key, attrName, attrValue);
        }
    }
}
