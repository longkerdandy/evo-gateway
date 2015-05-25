package com.github.longkerdandy.evo.adapter.wemo.storage;

import com.github.longkerdandy.evo.api.storage.RedisStorage;

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
}
