package com.github.longkerdandy.evo.adapter.hue.mqtt;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.TimerTask;

/**
 * Mqtt Connection Guard
 * Re-connect to the broker if connect lost
 */
public class MqttGuard extends TimerTask {

    // Logger
    private static final Logger logger = LoggerFactory.getLogger(MqttGuard.class);

    private MqttListener mqtt;      // MQTT Listener Instance

    public MqttGuard(MqttListener mqtt) {
        this.mqtt = mqtt;
    }

    @Override
    public void run() {
        try {
            this.mqtt.connect();
        } catch (MqttException e) {
            logger.debug("Can't connect to mqtt broker: {}", ExceptionUtils.getMessage(e));
        }
    }
}
