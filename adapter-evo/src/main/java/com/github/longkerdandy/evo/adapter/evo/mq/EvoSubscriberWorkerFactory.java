package com.github.longkerdandy.evo.adapter.evo.mq;

import com.github.longkerdandy.evo.adapter.evo.tcp.TcpClient;
import com.github.longkerdandy.evo.api.mq.SubscriberWorkerFactory;

/**
 * Message Queue Subscriber Worker Factory for Evolution Adapter
 */
public class EvoSubscriberWorkerFactory implements SubscriberWorkerFactory<EvoSubscriberWorker> {

    private final TcpClient client;

    public EvoSubscriberWorkerFactory(TcpClient client) {
        this.client = client;
    }

    @Override
    public EvoSubscriberWorker createWorker() {
        return new EvoSubscriberWorker(this.client);
    }
}
