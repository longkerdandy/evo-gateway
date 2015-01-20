package com.github.longkerdandy.evo.api.message;

import com.github.longkerdandy.evo.api.protocol.MessageType;
import com.github.longkerdandy.evo.api.protocol.Protocol;
import com.github.longkerdandy.evo.api.protocol.QoS;

import java.util.Map;
import java.util.UUID;

/**
 * Message Factory
 */
public class MessageFactory {

    /**
     * Create a default message
     *
     * @param msgType Message Type
     * @param from    Device ID (who send this message)
     * @param qos     QoS
     * @param payload Payload
     * @param <T>     Payload Message Class
     * @return Message with Payload
     */
    protected static <T> Message<T> newMessage(String msgType, String from, int qos, T payload) {
        Message<T> msg = new Message<>();
        msg.setMsgId(UUID.randomUUID().toString());     // Random UUID as Message Id
        msg.setMsgType(msgType);                        // Message Type
        msg.setFrom(from);                              // Device ID (who send this message)
        // msg.setTo();
        msg.setQos(qos);                                // QoS
        msg.setTimestamp(System.currentTimeMillis());   // Current time as Timestamp
        msg.setPayload(payload);                        // Payload
        return msg;
    }

    /**
     * Create a new Message<OnlineMessage>
     *
     * @param from        Device ID (who send this message)
     * @param description Device's Description Id
     * @param attributes  Device current Attributes
     * @return Message<OnlineMessage>
     */
    public static Message<OnlineMessage> newOnlineMessage(String from, String description, Map<String, Object> attributes) {
        OnlineMessage online = new OnlineMessage();
        online.setPv(Protocol.VERSION_1_0);     // Protocol Version
        online.setDescId(description);          // Description Id
        online.setAttributes(attributes);       // Attributes
        return newMessage(MessageType.ONLINE, from, QoS.LEAST_ONCE, online);
    }

    /**
     * Create a new Message<OfflineMessage>
     *
     * @param from Device ID (who send this message)
     * @return Message<OfflineMessage>
     */
    public static Message<OfflineMessage> newOfflineMessage(String from) {
        OfflineMessage disconnect = new OfflineMessage();
        return newMessage(MessageType.OFFLINE, from, QoS.LEAST_ONCE, disconnect);
    }

    /**
     * Create a new Message<TriggerMessage>
     *
     * @param from           Device ID (who send this message)
     * @param triggerId      Trigger Id
     * @param overridePolicy Attributes Override Policy
     * @param attributes     Attributes
     * @return Message<TriggerMessage>
     */
    public static Message<TriggerMessage> newTriggerMessage(String from, String triggerId, int overridePolicy, Map<String, Object> attributes) {
        TriggerMessage trigger = new TriggerMessage();
        trigger.setTriggerId(triggerId);
        trigger.setOverridePolicy(overridePolicy);
        trigger.setAttributes(attributes);
        return newMessage(MessageType.TRIGGER, from, QoS.MOST_ONCE, trigger);
    }
}
