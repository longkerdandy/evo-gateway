package com.github.longkerdandy.evo.adapter.wemo.handler;

import com.github.longkerdandy.evo.adapter.wemo.storage.WeMoRedisStorage;
import com.github.longkerdandy.evo.adapter.wemo.storage.WeMoScheme;
import org.fourthline.cling.controlpoint.SubscriptionCallback;
import org.fourthline.cling.model.gena.CancelReason;
import org.fourthline.cling.model.gena.GENASubscription;
import org.fourthline.cling.model.message.UpnpResponse;
import org.fourthline.cling.model.meta.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * SubscriptionCallback for WeMo
 */
@SuppressWarnings("unused")
public abstract class WeMoSubscriptionCallback extends SubscriptionCallback {

    // Logger
    private static final Logger logger = LoggerFactory.getLogger(WeMoSubscriptionCallback.class);

    protected final String deviceId;              // Device Id
    protected final String subscriptionId;        // Subscription Id
    protected final WeMoRedisStorage storage;     // Storage

    protected WeMoSubscriptionCallback(String deviceId, String subscriptionId, WeMoRedisStorage storage, Service service) {
        super(service);
        this.deviceId = deviceId;
        this.subscriptionId = subscriptionId;
        this.storage = storage;
    }

    protected WeMoSubscriptionCallback(String deviceId, String subscriptionId, WeMoRedisStorage storage, Service service, int requestedDurationSeconds) {
        super(service, requestedDurationSeconds);
        this.deviceId = deviceId;
        this.subscriptionId = subscriptionId;
        this.storage = storage;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public String getSubscriptionId() {
        return subscriptionId;
    }

    @Override
    protected void failed(GENASubscription subscription, UpnpResponse responseStatus, Exception exception, String defaultMsg) {
        logger.info("Device {} subscription {} failed: {}", this.deviceId, this.subscriptionId, defaultMsg);
        checkAndEmptyGENA();
    }

    @Override
    protected void established(GENASubscription subscription) {
        logger.info("Device {} subscription {} established", this.deviceId, this.subscriptionId);
    }

    @Override
    protected void ended(GENASubscription subscription, CancelReason reason, UpnpResponse responseStatus) {
        logger.info("Device {} subscription {} ended with reason: {}", this.deviceId, this.subscriptionId, reason);
        checkAndEmptyGENA();
    }

    @Override
    protected void eventsMissed(GENASubscription subscription, int numberOfMissedEvents) {
        logger.info("Device {} subscription {} missed {} events", this.deviceId, this.subscriptionId, numberOfMissedEvents);
    }

    /**
     * Check if subscription id is the same then set to empty
     */
    protected void checkAndEmptyGENA() {
        if (this.subscriptionId.equals(this.storage.getDeviceGENA(this.deviceId, WeMoScheme.GENA_ID))) {
            this.storage.updateDeviceGENA(this.deviceId, "");
            logger.debug("Clear device {} subscription {}", this.deviceId, this.subscriptionId);
        }
    }
}
