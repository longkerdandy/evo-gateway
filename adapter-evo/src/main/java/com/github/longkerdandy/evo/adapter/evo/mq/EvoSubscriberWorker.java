package com.github.longkerdandy.evo.adapter.evo.mq;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.JedisPubSub;

/**
 * Message Queue Subscriber for Evolution Platform
 */
public class EvoSubscriberWorker extends JedisPubSub {

    // Logger
    private static final Logger logger = LoggerFactory.getLogger(EvoSubscriberWorker.class);
}
