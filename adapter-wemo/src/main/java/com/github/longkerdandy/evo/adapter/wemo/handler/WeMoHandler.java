package com.github.longkerdandy.evo.adapter.wemo.handler;

import com.github.longkerdandy.evo.api.message.Action;
import com.github.longkerdandy.evo.api.message.Message;
import org.fourthline.cling.model.meta.RemoteDevice;
import org.fourthline.cling.registry.Registry;

/**
 * Message/Event Handler for WeMo devices
 */
public interface WeMoHandler {

    /**
     * Get handler device model
     *
     * @return Device Model
     */
    String getModel();

    /**
     * Subscription to be executed when device added or updated
     *
     * @return SubscriptionCallback
     */
    WeMoSubscriptionCallback getDeviceSubscription(String deviceId, Registry registry, RemoteDevice device);

    /**
     * Forge and push Connect Message to message queue
     *
     * @param deviceId Device Id
     */
    void sendConnectMessage(String deviceId);

    /**
     * Forge and push Disconnect Message to message queue
     *
     * @param deviceId Device Id
     */
    void sendDisconnectMessage(String deviceId);

    /**
     * Received Action Message from message queue
     * Execute the action on device
     *
     * @param device Device Id
     * @param msg    Action Message
     */
    void executeActionMessage(RemoteDevice device, Message<Action> msg);
}
