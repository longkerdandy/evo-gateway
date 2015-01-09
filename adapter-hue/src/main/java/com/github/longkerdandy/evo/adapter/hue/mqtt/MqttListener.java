package com.github.longkerdandy.evo.adapter.hue.mqtt;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.longkerdandy.evo.adapter.hue.bridge.HueListener;
import com.github.longkerdandy.evo.api.message.Message;
import com.github.longkerdandy.evo.api.mqtt.Topic;
import com.github.longkerdandy.evo.api.util.JsonUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Message Queue (MQTT) Listener
 */
public class MqttListener {

    // Logger
    private static final Logger logger = LoggerFactory.getLogger(MqttListener.class);

    private MqttClient client;              // Paho MQTT Client
    private MqttConnectOptions conOpt;      // Paho MQTT Connect Options

    private HueListener hueListener;        // Hue Listener instance

    /**
     * Constructor
     *
     * @param brokerUri MQTT Broker Address
     * @param clientId  MQTT Client Id
     */
    public MqttListener(String brokerUri, String clientId) {
        // use MemoryPersistence
        MemoryPersistence persistence = new MemoryPersistence();
        try {
            // construct the object that contains connection parameters
            // such as clean session and LWAT
            this.conOpt = new MqttConnectOptions();
            this.conOpt.setCleanSession(false);

            // construct the MqttClient instance
            this.client = new MqttClient(brokerUri, clientId, persistence);

            // set this wrapper as the callback handler
            this.client.setCallback(null);
        } catch (MqttException e) {
            logger.error("Create new MqListener with exception: {}", ExceptionUtils.getMessage(e));
        }
    }

    public void setHueListener(HueListener hueListener) {
        this.hueListener = hueListener;
    }

    /**
     * Connect to broker
     *
     * @throws MqttException
     */
    public void connect() throws MqttException {
        if (!this.client.isConnected()) {
            this.client.connect(this.conOpt);
            logger.debug("Connected to the broker {}", this.client.getServerURI());
        }
    }

    /**
     * Performs a single publish
     * Since Hue is a device, it will always publish to topic "devices"
     *
     * @param qos       the qos to publish at
     * @param payload   the payload of the message to publish
     * @throws MqttException
     */
    public void publish(int qos, Message payload) throws MqttException, JsonProcessingException {

        // if not connected, drop the action
        if (!this.client.isConnected()) {
            return;
        }

        // write to json bytes
        byte[] bytes = JsonUtils.ObjectMapper.writeValueAsBytes(payload);

        // Create and configure a message
        MqttMessage message = new MqttMessage(bytes);
        message.setQos(qos);

        // Send the message to the server, control is not returned until
        // it has been delivered to the server meeting the specified
        // quality of service.
        this.client.publish(Topic.DEIVCES, message);
        logger.debug("Successful publish message {} from {}", payload.getMsgType(), payload.getFrom());
    }

    /**
     * Subscribe to a topic on an MQTT server
     * Once subscribed this method waits for the messages to arrive from the server
     * that match the subscription. It continues listening for messages until the enter key is
     * pressed.
     *
     * @param topicName to subscribe to (can be wild carded)
     * @param qos       the maximum quality of service to receive messages at for this subscription
     * @throws MqttException
     */
    public void subscribe(String topicName, int qos) throws MqttException {

        // if not connected, drop the action
        if (!this.client.isConnected()) {
            return;
        }

        // Subscribe to the requested topic
        // The QoS specified is the maximum level that messages will be sent to the client at.
        // For instance if QoS 1 is specified, any messages originally published at QoS 2 will
        // be downgraded to 1 when delivering to the client but messages published at 1 and 0
        // will be received at the same level they were published at.
        client.subscribe(topicName, qos);
    }
}
