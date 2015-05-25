package com.github.longkerdandy.evo.api.storage;

/**
 * Redis Database Scheme
 */
public class Scheme {

    // Adapter Key : 'adapters:{id}'  Type : Hash
    // Sub (Hash) Key
    // Key : 'id'        Adapter id
    // Key : 'name'      Adapter name (Description)
    // Key : 'version'   Adapter software version
    // Key : 'callback'  Adapter message queue callback topic (Subscribed)
    public static String ADAPTER(String adapterId) {
        return "adapters:" + adapterId;
    }
    public static final String ADAPTER_ID = "id";
    public static final String ADAPTER_NAME = "name";
    public static final String ADAPTER_VERSION = "version";
    public static final String ADAPTER_CALLBACK = "callback";

    public static final String DEVICE_MAPPING = "devices:mapping";

    public static String DEVICE_CONN(String deviceId) {
        return "devices:" + deviceId + ":conn";
    }

    public static String DEVICE_ATTR(String deviceId) {
        return "devices:" + deviceId + ":attr";
    }

    private Scheme() {
    }
}
