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
     * Create a new Message<ConnectMessage>
     *
     * @param from        Device ID (who send this message)
     * @param description Device's Description Id
     * @param attributes  Device current Attributes
     * @return Message<ConnectMessage>
     */
    public static Message<ConnectMessage> newConnectMessage(String from, String description, Map<String, Object> attributes) {
        ConnectMessage connect = new ConnectMessage();
        connect.setProtocolVersion(Protocol.VERSION_1_0);       // Protocol Version
        connect.setDescription(description);                    // Description Id
        connect.setAttributes(attributes);                      // Attributes
        return newMessage(MessageType.CONNECT, from, QoS.LEAST_ONCE, connect);
    }

    /**
     * Create a new Message<DisconnectMessage>
     *
     * @param from        Device ID (who send this message)
     * @return Message<DisconnectMessage>
     */
    public static Message<DisconnectMessage> newDisconnectMessage(String from) {
        DisconnectMessage disconnect = new DisconnectMessage();
        return newMessage(MessageType.DISCONNECT, from, QoS.MOST_ONCE, disconnect);
    }
}
