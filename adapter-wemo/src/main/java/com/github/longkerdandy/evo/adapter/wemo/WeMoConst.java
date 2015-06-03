package com.github.longkerdandy.evo.adapter.wemo;

import com.github.longkerdandy.evo.adapter.wemo.handler.WeMoHandler;
import com.github.longkerdandy.evo.api.util.UuidUtils;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

/**
 * WeMo Descriptor Definition
 */
public class WeMoConst {

    // Model
    public static final String MODEL_SWITCH = "switch";     // WeMo device using 'Socket'
    public static final String MODEL_MOTION = "motion";
    // Trigger
    public static final String TRIGGER_SWITCH_STATE_CHANGED = "state_changed";
    // Action
    public static final String ACTION_SWITCH_SET_STATE = "set_state";
    // Attribute
    public static final String ATTRIBUTE_NAME = "name";
    public static final String ATTRIBUTE_MANUFACTURER = "manufacturer";
    public static final String ATTRIBUTE_MODEL = "model";
    public static final String ATTRIBUTE_SERIAL_NUMBER = "sn";              // adapter only
    public static final String ATTRIBUTE_UDN = "udn";                       // adapter only
    public static final String ATTRIBUTE_DESCRIPTOR_URL = "desc_url";       // adapter only
    public static final String ATTRIBUTE_SWITCH_STATE = "state";

    private WeMoConst() {
    }

    /**
     * Generate WeMo Device Id from serial number
     *
     * @param serialNumber Serial Number
     * @return Device Id
     */
    public static String deviceId(String serialNumber) {
        String deviceId = null;
        try {
            URI uri = new URI("https://github.com/longkerdandy/evo-gateway/wemo-adapter?sn=" + serialNumber);
            deviceId = UuidUtils.shortUuid(uri);
        } catch (URISyntaxException ignore) {
            // never happens
        }
        return deviceId;
    }

    /**
     * Transfer WeMo default model name to user friendly model name
     */
    public static String model(String modelName) {
        switch (modelName.toUpperCase()) {
            case "SOCKET":
                return WeMoConst.MODEL_SWITCH;
            default:
                return null;
        }
    }

    /**
     * Find handler matching device model
     */
    public static WeMoHandler findHandlerByModel(List<WeMoHandler> handlers, String model) {
        for (WeMoHandler handler : handlers) {
            if (handler.getModel().equalsIgnoreCase(model))
                return handler;
        }
        throw new IllegalArgumentException("Can't find matching handler for model " + model);
    }
}
