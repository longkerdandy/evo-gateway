package com.github.longkerdandy.evo.adapter.wemo.storage;

import com.github.longkerdandy.evo.adapter.wemo.WeMoAdapter;
import com.github.longkerdandy.evo.api.storage.Scheme;

/**
 * Redis Database Scheme for WeMo
 */
public class WeMoScheme {

    public static final String GENA_ID = "id";
    public static final String GENA_TIMESTAMP = "timestamp";

    private WeMoScheme() {
    }

    // Device GENA Key : 'adapters:{adapterId}:gena:{deviceId}'  Type : Hash
    // Key : 'id'          Value : Subscription id
    // Key : 'timestamp'   Value : Subscription timestamp
    public static String DEVICE_SUB(String deviceId) {
        return Scheme.ADAPTER(WeMoAdapter.ID) + ":gena:" + deviceId;
    }
}
