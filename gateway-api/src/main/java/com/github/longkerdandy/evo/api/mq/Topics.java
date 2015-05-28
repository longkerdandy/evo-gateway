package com.github.longkerdandy.evo.api.mq;

/**
 * Message Topics
 */
public class Topics {

    public static String DEVICE_ADAPTER(String adapterId) {
        return "adapters:" + adapterId;
    }

    public static String CLOUD_ADAPTER = "adapters:cloud";
}
