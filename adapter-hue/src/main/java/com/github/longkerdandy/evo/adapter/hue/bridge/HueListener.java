package com.github.longkerdandy.evo.adapter.hue.bridge;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.longkerdandy.evo.adapter.hue.message.HueMessageFactory;
import com.github.longkerdandy.evo.adapter.hue.mqtt.MqttListener;
import com.github.longkerdandy.evo.api.message.ConnectMessage;
import com.github.longkerdandy.evo.api.message.DisconnectMessage;
import com.github.longkerdandy.evo.api.message.Message;
import com.github.longkerdandy.evo.api.protocol.QoS;
import com.philips.lighting.hue.sdk.PHAccessPoint;
import com.philips.lighting.hue.sdk.PHHueSDK;
import com.philips.lighting.hue.sdk.PHMessageType;
import com.philips.lighting.hue.sdk.PHSDKListener;
import com.philips.lighting.hue.sdk.heartbeat.PHHeartbeatManager;
import com.philips.lighting.model.PHBridge;
import com.philips.lighting.model.PHHueParsingError;
import com.philips.lighting.model.PHLight;
import com.philips.lighting.model.PHLightState;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * PHSDKListener implementation
 */
public class HueListener implements PHSDKListener {

    // Logger
    private static final Logger logger = LoggerFactory.getLogger(HueListener.class);

    private final String userName;              // User Name, should be Gateway's Device Id
    private final PHHueSDK hue;                 // HueSDK instance
    private final MqttListener mqttListener;    // MQTT Listener instance
    private PHAccessPoint bridgeAddress;        // Current Hue Bridge Address
    private PHBridge bridge;                    // Current Hue Bridge Object
    private PHHeartbeatManager hb;              // Heartbeat runs at regular intervals and update the Bridge Resources cache.
    private boolean isConnected;                // Is connected to the bridge
    private Map<String, PHLightState> states;   // Light Id : State Mapping

    public HueListener(String userName, PHHueSDK hue, MqttListener mqttListener) {
        this.userName = userName;
        this.hue = hue;
        this.mqttListener = mqttListener;
        this.hb = PHHeartbeatManager.getInstance();
        this.states = new HashMap<>();
    }

    @Override
    public void onCacheUpdated(List<Integer> notify, PHBridge phBridge) {
        // logger.trace("Received onCacheUpdated event");
        // Here you receive notifications that the BridgeResource Cache was updated.
        // Use the PHMessageType to check which cache was updated.
        if (notify.contains(PHMessageType.LIGHTS_CACHE_UPDATED)) {
            logger.trace("Received Lights onCacheUpdated event");

            // replace states
            Map<String, PHLightState> newStates = new HashMap<>();

            // Loop and compare light's state, see which one has changed
            List<PHLight> lights = this.bridge.getResourceCache().getAllLights();
            for (PHLight light : lights) {
                PHLightState currentState = light.getLastKnownLightState();
                PHLightState lastState = this.states.get(light.getIdentifier());
                if (lastState == null) {
                    if (currentState.isReachable()) {
                        logger.debug("Device online Light {} hue:{}", light.getIdentifier(), currentState.getHue());
                        // publish message
                        Message<ConnectMessage> msg = HueMessageFactory.newConnectMessage(light);
                        tryPublish(QoS.LEAST_ONCE , msg);
                    }
                } else if (lastState.isReachable() && !currentState.isReachable()) {
                    logger.debug("Device offline Light {}", light.getIdentifier());
                    // publish message
                    Message<DisconnectMessage> msg = HueMessageFactory.newDisconnectMessage(light);
                    tryPublish(QoS.LEAST_ONCE , msg);
                } else if (!lastState.isReachable() && currentState.isReachable()) {
                    logger.debug("Device online Light {} hue:{}", light.getIdentifier(), currentState.getHue());
                    // publish message
                    Message<ConnectMessage> msg = HueMessageFactory.newConnectMessage(light);
                    tryPublish(QoS.LEAST_ONCE , msg);
                } else if (lastState.isReachable() && currentState.isReachable() && !lastState.equals(currentState)) {
                    logger.debug("Device state change Light {} hue:{}", light.getIdentifier(), currentState.getHue());
                }
                newStates.put(light.getIdentifier(), currentState);
            }

            this.states = newStates;
        }
    }

    @Override
    public void onBridgeConnected(PHBridge phBridge) {
        logger.trace("Received onBridgeConnected event");
        this.isConnected = true;
        // enable heartbeat
        if (this.bridge != null) this.hb.disableAllHeartbeats(this.bridge);
        this.bridge = phBridge;
        this.hb.enableLightsHeartbeat(this.bridge, PHHueSDK.HB_INTERVAL);

        List<PHLight> lights = this.bridge.getResourceCache().getAllLights();
        for (PHLight light : lights) {
            PHLightState lightState = light.getLastKnownLightState();
            // save id : state mapping
            this.states.put(light.getIdentifier(), lightState);
            // publish message if light is online
            if (lightState.isReachable()) {
                logger.debug("Device online Light {} hue:{}", light.getIdentifier(), lightState.getHue());
                Message<ConnectMessage> msg = HueMessageFactory.newConnectMessage(light);
                tryPublish(QoS.LEAST_ONCE , msg);
            }
        }
    }

    @Override
    public void onAuthenticationRequired(PHAccessPoint phAccessPoint) {
        logger.trace("Received onAuthenticationRequired event");
    }

    @Override
    public void onAccessPointsFound(List<PHAccessPoint> bridgeAddress) {
        // logger.trace("Received onAccessPointsFound event");
        // Handle your bridge search results here.
        // Typically if multiple results are returned you will want to display them in a list and let the user select their bridge.
        // If one is found you may opt to connect automatically to that bridge.
        if (!bridgeAddress.isEmpty()) {
            PHAccessPoint newBridge = bridgeAddress.get(0);
            if (this.bridgeAddress == null
                    || !this.bridgeAddress.getIpAddress().equals(newBridge.getIpAddress())
                    || !this.bridgeAddress.getMacAddress().equals(newBridge.getMacAddress())) {
                this.bridgeAddress = newBridge;
                logger.debug("Found new Hue Bridge ip:{} mac:{} username:{}",
                        this.bridgeAddress.getIpAddress(), this.bridgeAddress.getMacAddress(), this.bridgeAddress.getUsername());
                this.bridgeAddress.setUsername(this.userName);
            }
        }
        // always try to re-connect to the bridge
        if (!isConnected) {
            logger.debug("Try to connect to the bridge {}", this.bridgeAddress.getIpAddress());
            this.hue.connect(this.bridgeAddress);
        }
    }

    @Override
    public void onError(int i, String s) {
        logger.trace("Received onError event");
    }

    @Override
    public void onConnectionResumed(PHBridge phBridge) {
        // logger.trace("Received onConnectionResumed event");
        this.isConnected = true;
    }

    @Override
    public void onConnectionLost(PHAccessPoint phAccessPoint) {
        logger.trace("Received onConnectionLost event");
        this.isConnected = false;
    }

    @Override
    public void onParsingErrors(List<PHHueParsingError> list) {
        logger.trace("Received onParsingErrors event");
    }


    /**
     * Try to publish message to mqtt topic
     *
     * @param qos     QoS
     * @param payload Payload
     */
    protected void tryPublish(int qos, Message payload) {
        try {
            this.mqttListener.publish(qos, payload);
        } catch (MqttException e) {
            logger.warn("MQTT publish exception: {}", ExceptionUtils.getMessage(e));
        } catch (JsonProcessingException e) {
            logger.error("Json process exception: {}", ExceptionUtils.getMessage(e));
        }
    }
}
