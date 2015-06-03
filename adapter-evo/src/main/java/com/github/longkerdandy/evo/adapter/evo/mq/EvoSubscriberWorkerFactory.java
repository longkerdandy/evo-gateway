package com.github.longkerdandy.evo.adapter.evo.mq;

import com.github.longkerdandy.evo.api.mq.SubscriberWorkerFactory;

/**
 * Message Queue Subscriber Worker Factory for Evolution Platform
 */
public class EvoSubscriberWorkerFactory implements SubscriberWorkerFactory<EvoSubscriberWorker> {

    @Override
    public EvoSubscriberWorker createWorker() {
        return new EvoSubscriberWorker();
    }
}
