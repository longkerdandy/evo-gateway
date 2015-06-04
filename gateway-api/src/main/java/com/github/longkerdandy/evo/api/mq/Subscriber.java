package com.github.longkerdandy.evo.api.mq;

import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Message Queue Subscriber
 */
@SuppressWarnings("unused")
public class Subscriber {

    // Logger
    private static final Logger logger = LoggerFactory.getLogger(Subscriber.class);

    // Redis Client
    private final Jedis jedis;
    // Worker Threads
    private final ExecutorService executor;

    public Subscriber() {
        this("localhost", 6379);
    }

    public Subscriber(String host, int port) {
        this.jedis = new Jedis(host, port);
        this.executor = Executors.newSingleThreadExecutor();
    }

    /**
     * Close subscriber
     */
    public void close() {
        if (this.jedis != null) this.jedis.close();
        if (this.executor != null) this.executor.shutdown();
    }

    /**
     * Subscribe to message queue topic
     *
     * @param topic   Topic
     * @param factory Worker Factory
     */
    public void subscribe(String topic, SubscriberWorkerFactory factory) {
        // jedis subscribe method will block
        this.executor.execute(() -> jedis.subscribe(factory.createWorker(), topic));
        logger.debug("Successful subscribed to mq topic {}", topic);
    }

    /**
     * Subscribe to message queue topics
     *
     * @param topics  Topics
     * @param factory Worker Factory
     */
    public void subscribe(String[] topics, SubscriberWorkerFactory factory) {
        // jedis subscribe method will block
        this.executor.execute(() -> jedis.subscribe(factory.createWorker(), topics));
        logger.debug("Successful subscribed to mq topic {}", ArrayUtils.toString(topics));
    }
}
