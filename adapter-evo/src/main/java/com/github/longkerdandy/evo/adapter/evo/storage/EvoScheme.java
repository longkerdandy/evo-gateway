package com.github.longkerdandy.evo.adapter.evo.storage;

import com.github.longkerdandy.evo.adapter.evo.EvoAdapter;
import com.github.longkerdandy.evo.api.storage.Scheme;

/**
 * Redis Database Scheme for Evolution Platform
 */
public class EvoScheme {

    private EvoScheme() {
    }

    // Cached Message Key : 'adapters:{adapterId}:cached:{msgId}'  Type : String
    // Value : Message Json String
    public static String CACHED_MSG(String msgId) {
        return Scheme.ADAPTER(EvoAdapter.ID) + ":cached:" + msgId;
    }
}
