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

    /**
     * Generate WeMo Device Id from serial number
     *
     * @param serialNumber Serial Number
     * @return Device Id
     */
    public static String DEVICE_ID(String serialNumber) {
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
     * Get handler matching device model
     */
    public static WeMoHandler getHandlerByModel(List<WeMoHandler> handlers, String model) {
        for (WeMoHandler handler : handlers) {
            if (handler.getModel().equalsIgnoreCase(model))
                return handler;
        }
        return null;
    }

    // Model
    public static final String MODEL_SWITCH = "switch";     // WeMo device using 'Socket'
    public static final String MODEL_MOTION = "motion";

    // Trigger
    public static final String TRIGGER_SWITCH_ON = "switch_on";
    public static final String TRIGGER_SWITCH_OFF = "switch_off";

    // Attribute
    public static final String ATTRIBUTE_NAME = "name";
    public static final String ATTRIBUTE_MANUFACTURER = "manufacturer";
    public static final String ATTRIBUTE_MODEL = "model";
    public static final String ATTRIBUTE_SERIAL_NUMBER = "sn";              // adapter only
    public static final String ATTRIBUTE_DESCRIPTOR_URL = "desc_url";       // adapter only
    public static final String ATTRIBUTE_SWITCH_STATE = "state";

    private WeMoConst() {
    }
}
