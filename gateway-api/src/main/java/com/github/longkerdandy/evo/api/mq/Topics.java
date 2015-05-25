package com.github.longkerdandy.evo.api.mq;

/**
 * Message Topics
 */
public class Topics {

    public static String ADAPTER(String adapterId) {
        return "adapters:" + adapterId;
    }
}
