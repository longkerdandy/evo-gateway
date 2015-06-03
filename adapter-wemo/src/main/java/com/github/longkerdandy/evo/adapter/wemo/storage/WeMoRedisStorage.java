package com.github.longkerdandy.evo.adapter.wemo.storage;

import com.github.longkerdandy.evo.api.storage.RedisStorage;
import redis.clients.jedis.Jedis;

import java.util.HashMap;
import java.util.Map;

/**
 * Redis Database Access Layer for WeMo Adapter
 */
public class WeMoRedisStorage extends RedisStorage {

    public WeMoRedisStorage() {
        this("localhost", 6379);
    }

    public WeMoRedisStorage(String host, int port) {
        super(host, port);
    }

    /**
     * Get device subscription by device id
     *
     * @param deviceId Device Id
     * @return Subscription
     */
    public Map<String, String> getDeviceSub(String deviceId) {
        try (Jedis jedis = this.jedisPool.getResource()) {
            String key = WeMoScheme.DEVICE_SUB(deviceId);
            return jedis.hgetAll(key);
        }
    }

    /**
     * Get device subscription field by device id
     *
     * @param deviceId Device Id
     * @param field    Subscription Field
     * @return Subscription Field Value
     */
    public String getDeviceSub(String deviceId, String field) {
        try (Jedis jedis = this.jedisPool.getResource()) {
            String key = WeMoScheme.DEVICE_SUB(deviceId);
            return jedis.hget(key, field);
        }
    }

    /**
     * Update device subscription
     *
     * @param deviceId       Device Id
     * @param subscriptionId Subscription data to be updated
     */
    public void updateDeviceSub(String deviceId, String subscriptionId) {
        try (Jedis jedis = this.jedisPool.getResource()) {
            Map<String, String> sub = new HashMap<>();
            sub.put(WeMoScheme.DEVICE_SUB_ID, subscriptionId);
            sub.put(WeMoScheme.DEVICE_SUB_TIMESTAMP, String.valueOf(System.currentTimeMillis()));
            String key = WeMoScheme.DEVICE_SUB(deviceId);
            jedis.hmset(key, sub);
        }
    }
}