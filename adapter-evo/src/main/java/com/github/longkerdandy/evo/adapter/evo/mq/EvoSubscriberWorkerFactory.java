package com.github.longkerdandy.evo.adapter.evo.mq;

import com.github.longkerdandy.evo.adapter.evo.tcp.TcpClientHandler;
import com.github.longkerdandy.evo.api.mq.SubscriberWorkerFactory;

/**
 * Message Queue Subscriber Worker Factory for Evolution Adapter
 */
public class EvoSubscriberWorkerFactory implements SubscriberWorkerFactory<EvoSubscriberWorker> {

    private final TcpClientHandler handler;

    public EvoSubscriberWorkerFactory(TcpClientHandler handler) {
        this.handler = handler;
    }

    @Override
    public EvoSubscriberWorker createWorker() {
        return new EvoSubscriberWorker(this.handler);
    }
}
