package com.github.longkerdandy.evo.adapter.wemo.mq;

import com.github.longkerdandy.evo.adapter.wemo.handler.WeMoHandler;
import com.github.longkerdandy.evo.adapter.wemo.storage.WeMoRedisStorage;
import com.github.longkerdandy.evo.api.mq.SubscriberWorkerFactory;
import org.fourthline.cling.UpnpService;

import java.util.List;

/**
 * Message Queue Subscriber Worker Factory for WeMo
 */
public class WeMoSubscriberWorkerFactory implements SubscriberWorkerFactory<WeMoSubscriberWorker> {

    private final UpnpService upnpService;              // UPnP service
    private final WeMoRedisStorage storage;             // Storage
    private final List<WeMoHandler> handlers;           // Handlers

    public WeMoSubscriberWorkerFactory(UpnpService upnpService, WeMoRedisStorage storage, List<WeMoHandler> handlers) {
        this.upnpService = upnpService;
        this.storage = storage;
        this.handlers = handlers;
    }

    @Override
    public WeMoSubscriberWorker createWorker() {
        return new WeMoSubscriberWorker(this.upnpService, this.storage, this.handlers);
    }
}
