package com.github.longkerdandy.evo.api.mq;

import redis.clients.jedis.JedisPubSub;

/**
 * Message Queue Subscriber Worker Factory
 */
public interface SubscriberWorkerFactory<W extends JedisPubSub> {

    /**
     * Create a new redis message queue subscriber worker
     *
     * @return Subscriber Worker
     */
    W createWorker();
}
