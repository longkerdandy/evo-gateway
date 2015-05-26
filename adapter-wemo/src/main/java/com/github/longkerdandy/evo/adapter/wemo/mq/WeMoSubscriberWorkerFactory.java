package com.github.longkerdandy.evo.adapter.wemo.mq;

import com.github.longkerdandy.evo.api.mq.SubscriberWorkerFactory;

/**
 * Message Queue Subscriber Worker Factory for WeMo
 */
public class WeMoSubscriberWorkerFactory implements SubscriberWorkerFactory<WeMoSubscriberWorker> {

    @Override
    public WeMoSubscriberWorker createWorker() {
        return new WeMoSubscriberWorker();
    }
}
