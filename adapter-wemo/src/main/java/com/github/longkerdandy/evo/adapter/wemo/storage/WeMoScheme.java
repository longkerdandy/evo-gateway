package com.github.longkerdandy.evo.adapter.wemo.storage;

/**
 * Redis Database Scheme for WeMo
 */
public class WeMoScheme {

    // Device Subscription Key : 'devices:{id}:sub'  Type : Hash
    // Key : 'sub_state'       Value : Subscription id
    // Key : 'sub_timestamp'   Value : Subscription timestamp
    public static String DEVICE_SUB(String deviceId) {
        return "devices:" + deviceId + ":sub";
    }
    public static final String DEVICE_SUB_ID = "sub_state";
    public static final String DEVICE_SUB_TIMESTAMP = "sub_timestamp";

    private WeMoScheme() {
    }
}
