package com.github.longkerdandy.evo.api.mqtt;

/**
 * MQTT Topic
 */
public class Topic {

    public static final String DEVICES = "devices";             // Device should publish message to this topic
    public static final String CONTROLLERS = "controllers";     // Controller should publish message to this topic

    /**
     * Generate client's subscribe topic
     *
     * @param clientId Client's Id
     * @return Subscribe Topic
     */
    public static String subscribeTopic(String clientId) {
        return "device:" + clientId;
    }
}
