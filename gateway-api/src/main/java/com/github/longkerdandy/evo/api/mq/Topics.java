package com.github.longkerdandy.evo.api.mq;

/**
 * Message Topics
 */
public class Topics {

    // Topic for device event
    public static String DEVICE_EVENT = "adapters:event";

    // Topic for each adapter
    public static String DEVICE_ADAPTER(String adapterId) {
        return "adapters:" + adapterId;
    }
}
