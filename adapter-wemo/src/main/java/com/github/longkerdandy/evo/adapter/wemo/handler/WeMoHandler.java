package com.github.longkerdandy.evo.adapter.wemo.handler;

import org.fourthline.cling.UpnpService;
import org.fourthline.cling.model.meta.RemoteDevice;
import org.fourthline.cling.registry.Registry;

/**
 * Message/Event Handler for WeMo devices
 */
public interface WeMoHandler {

    /**
     * Get handler device type
     *
     * @return Device Type
     */
    String getType();

    /**
     * Called when complete metadata of a newly discovered device is available.
     *
     * @param upnpService The Cling UPnP service stack
     * @param registry    The Cling registry of all devices and services know to the local UPnP stack.
     * @param device      A validated and hydrated device metadata graph, with complete service metadata.
     */
    void deviceAdded(UpnpService upnpService, Registry registry, RemoteDevice device);

    /**
     * Called when a discovered device's expiration timestamp is updated.
     * <p>
     * This is a signal that a device is still alive and you typically don't have to react to this
     * event. You will be notified when a device disappears through timeout.
     * </p>
     *
     * @param upnpService The Cling UPnP service stack
     * @param registry    The Cling registry of all devices and services know to the local UPnP stack.
     * @param device      A validated and hydrated device metadata graph, with complete service metadata.
     */
    void deviceUpdated(UpnpService upnpService, Registry registry, RemoteDevice device);

    /**
     * Called when a previously discovered device disappears.
     * <p>
     * This method will also be called when a discovered device did not update its expiration timeout
     * and has been been removed automatically by the local registry. This method will not be called
     * when the UPnP stack is shutting down.
     * </p>
     *
     * @param upnpService The Cling UPnP service stack
     * @param registry    The Cling registry of all devices and services know to the local UPnP stack.
     * @param device      A validated and hydrated device metadata graph, with complete service metadata.
     */
    void deviceRemoved(UpnpService upnpService, Registry registry, RemoteDevice device);
}
