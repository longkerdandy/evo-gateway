package com.github.longkerdandy.evo.adapter.wemo.upnp;

import com.github.longkerdandy.evo.adapter.wemo.WeMoConst;
import com.github.longkerdandy.evo.adapter.wemo.handler.WeMoHandler;
import com.github.longkerdandy.evo.adapter.wemo.handler.WeMoSubscriptionCallback;
import com.github.longkerdandy.evo.adapter.wemo.storage.WeMoRedisStorage;
import com.github.longkerdandy.evo.adapter.wemo.storage.WeMoScheme;
import com.github.longkerdandy.evo.api.storage.Scheme;
import com.github.longkerdandy.evo.api.util.UuidUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.fourthline.cling.UpnpService;
import org.fourthline.cling.model.meta.LocalDevice;
import org.fourthline.cling.model.meta.RemoteDevice;
import org.fourthline.cling.registry.Registry;
import org.fourthline.cling.registry.RegistryListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * RegistryListener for WeMo Devices
 */
public class WeMoRegistryListener implements RegistryListener {

    // Logger
    private static final Logger logger = LoggerFactory.getLogger(WeMoRegistryListener.class);

    private final UpnpService upnpService;              // UPnP service
    private final WeMoRedisStorage storage;             // Storage
    private final List<WeMoHandler> handlers;           // Handlers

    public WeMoRegistryListener(UpnpService upnpService, WeMoRedisStorage storage, List<WeMoHandler> handlers) {
        this.upnpService = upnpService;
        this.storage = storage;
        this.handlers = handlers;
    }

    @Override
    public void remoteDeviceDiscoveryStarted(Registry registry, RemoteDevice device) {
        logger.debug("Discovery started: {}", device.getDisplayString());
    }

    @Override
    public void remoteDeviceDiscoveryFailed(Registry registry, RemoteDevice device, Exception ex) {
        logger.debug("Discovery failed: {} {}", device.getDisplayString(), ExceptionUtils.getMessage(ex));
    }

    @Override
    public void remoteDeviceAdded(Registry registry, RemoteDevice device) {
        logger.debug("Remote device added: {}", device.getDisplayString());

        String deviceId = getDeviceId(device);
        WeMoHandler handler = WeMoConst.getHandlerByModel(this.handlers, getModel(device.getDetails().getModelDetails().getModelName()));
        if (handler == null) return;

        // update device
        this.storage.updateDeviceConn(deviceId, "1");
        updateDeviceAttr(deviceId, device);

        // update and invoke subscription
        WeMoSubscriptionCallback subscription = handler.getDeviceSubscription(deviceId, registry, device);
        this.storage.updateDeviceSub(deviceId, subscription.getSubscriptionId());
        this.upnpService.getControlPoint().execute(subscription);
    }

    @Override
    public void remoteDeviceUpdated(Registry registry, RemoteDevice device) {
        logger.debug("Remote device updated: {}", device.getDisplayString());

        String deviceId = getDeviceId(device);
        WeMoHandler handler = WeMoConst.getHandlerByModel(this.handlers, getModel(device.getDetails().getModelDetails().getModelName()));
        if (handler == null) return;

        // detected reconnect
        if (!device.getIdentity().getDescriptorURL().toString().equals(this.storage.getDeviceAttr(deviceId, WeMoConst.ATTRIBUTE_DESCRIPTOR_URL))) {
            logger.debug("Device {} description url changed, consider as reconnected", deviceId);

            // update device
            this.storage.updateDeviceConn(deviceId, "1");
            updateDeviceAttr(deviceId, device);

            // update and invoke subscription
            WeMoSubscriptionCallback subscription = handler.getDeviceSubscription(deviceId, registry, device);
            this.storage.updateDeviceSub(deviceId, subscription.getSubscriptionId());
            this.upnpService.getControlPoint().execute(subscription);

        } else if (checkSubFailed(deviceId)) {
            logger.debug("Device {} consider as subscription failed, create new subscription", deviceId);

            // update and invoke subscription
            WeMoSubscriptionCallback subscription = handler.getDeviceSubscription(deviceId, registry, device);
            this.storage.updateDeviceSub(deviceId, subscription.getSubscriptionId());
            this.upnpService.getControlPoint().execute(subscription);
        }
    }

    @Override
    public void remoteDeviceRemoved(Registry registry, RemoteDevice device) {
        logger.debug("Remote device removed: {}", device.getDisplayString());

        String deviceId = getDeviceId(device);
        WeMoHandler handler = WeMoConst.getHandlerByModel(this.handlers, getModel(device.getDetails().getModelDetails().getModelName()));
        if (handler == null) return;

        // update device
        this.storage.updateDeviceConn(deviceId, "0");
        this.storage.updateDeviceSub(deviceId, "");

        // send disconnect message
        handler.sendDisconnectMessage(deviceId);
    }

    @Override
    public void localDeviceAdded(Registry registry, LocalDevice device) {
        logger.debug("Local device added: {}", device.getDisplayString());
    }

    @Override
    public void localDeviceRemoved(Registry registry, LocalDevice device) {
        logger.debug("Local device removed: {}", device.getDisplayString());
    }

    @Override
    public void beforeShutdown(Registry registry) {
        logger.debug("Before shutdown, the registry has {} devices", registry.getDevices().size());
    }

    @Override
    public void afterShutdown() {
        logger.debug("Shutdown of registry complete!");
    }

    /**
     * Generate Device Id from RemoteDevice's serial number
     *
     * @param device RemoteDevice
     * @return Device Id
     */
    protected String getDeviceId(RemoteDevice device) {
        String deviceId = null;
        try {
            URI uri = new URI("https://github.com/longkerdandy/evo-gateway/wemo-adapter?sn=" + device.getDetails().getSerialNumber());
            deviceId = UuidUtils.shortUuid(uri);
        } catch (URISyntaxException ignore) {
            // never happens
        }
        return deviceId;
    }

    /**
     * Update device attribute in storage
     *
     * @param deviceId Device Id
     * @param device   RemoteDevice
     */
    protected void updateDeviceAttr(String deviceId, RemoteDevice device) {
        Map<String, String> attr = new HashMap<>();
        attr.put(WeMoConst.ATTRIBUTE_NAME, device.getDetails().getFriendlyName());
        attr.put(WeMoConst.ATTRIBUTE_MANUFACTURER, device.getDetails().getManufacturerDetails().getManufacturer());
        attr.put(WeMoConst.ATTRIBUTE_MODEL, getModel(device.getDetails().getModelDetails().getModelName()));
        attr.put(WeMoConst.ATTRIBUTE_SERIAL_NUMBER, device.getDetails().getSerialNumber());
        attr.put(WeMoConst.ATTRIBUTE_DESCRIPTOR_URL, device.getIdentity().getDescriptorURL().toString());
        this.storage.updateDeviceAttr(deviceId, attr);
    }

    /**
     * Transfer WeMo default model name to user friendly model name
     */
    protected String getModel(String modelName) {
        switch (modelName.toUpperCase()) {
            case "SOCKET":
                return WeMoConst.MODEL_SWITCH;
            default:
                return "unknown";
        }
    }

    /**
     * Check if device subscription considered failed
     *
     * @param deviceId Device Id
     * @return True if failed
     */
    protected boolean checkSubFailed(String deviceId) {
        if (StringUtils.isEmpty(this.storage.getDeviceSub(deviceId, WeMoScheme.DEVICE_SUB_ID))) {
            long subTs = NumberUtils.toLong(this.storage.getDeviceSub(deviceId, WeMoScheme.DEVICE_SUB_TIMESTAMP), 0);
            long connTs = NumberUtils.toLong(this.storage.getDeviceConn(deviceId, Scheme.DEVICE_CONN_TIMESTAMP), 0);
            if (subTs > connTs && System.currentTimeMillis() - subTs > 180000) {
                return true;
            }
        }
        return false;
    }
}
