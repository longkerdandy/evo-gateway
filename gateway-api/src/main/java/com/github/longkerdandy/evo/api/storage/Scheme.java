package com.github.longkerdandy.evo.api.storage;

/**
 * Redis Database Scheme
 */
public class Scheme {

    public static final String ADAPTER_ID = "id";
    public static final String ADAPTER_NAME = "name";
    public static final String ADAPTER_VERSION = "version";
    public static final String ADAPTER_CALLBACK = "callback";
    // Device Adapter Mapping Key : 'devices:mapping'  Type : Hash
    // Key : Device Id       Value : Adapter Id
    public static final String DEVICE_MAPPING = "devices:mapping";
    public static final String DEVICE_CONN_STATE = "conn_state";
    public static final String DEVICE_CONN_TIMESTAMP = "conn_timestamp";

    private Scheme() {
    }

    // Adapter Key : 'adapters:{id}'  Type : Hash
    // Key : 'id'       Value : Adapter Id
    // Key : 'name'     Value : Adapter name (Description)
    // Key : 'version'  Value : Adapter software version
    // Key : 'callback' Value : Adapter message queue callback topic (Subscribed)
    public static String ADAPTER(String adapterId) {
        return "adapters:" + adapterId;
    }

    // Device Connection Key : 'devices:{id}:conn'  Type : Hash
    // Key : 'conn_state'       Value : Connection state, 0 means disconnected. 1 means connected
    // Key : 'conn_timestamp'   Value : Connection timestamp
    public static String DEVICE_CONN(String deviceId) {
        return "devices:" + deviceId + ":conn";
    }

    // Device Attribute Key : 'devices:{id}:attr'  Type : Hash
    // Key : Attribute Name       Value : Attribute Value
    public static String DEVICE_ATTR(String deviceId) {
        return "devices:" + deviceId + ":attr";
    }
}
