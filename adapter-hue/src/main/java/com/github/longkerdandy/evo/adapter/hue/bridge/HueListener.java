package com.github.longkerdandy.evo.adapter.hue.bridge;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.longkerdandy.evo.adapter.hue.constant.Description;
import com.github.longkerdandy.evo.adapter.hue.message.HueMessageFactory;
import com.github.longkerdandy.evo.adapter.hue.mqtt.MqttListener;
import com.github.longkerdandy.evo.api.message.*;
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

            // replace with current states
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
                        Message<OnlineMessage> msg = HueMessageFactory.newOnlineMessage(this.bridgeAddress, light);
                        tryPublish(msg);
                    }
                } else if (lastState.isReachable() && !currentState.isReachable()) {
                    logger.debug("Device offline Light {}", light.getIdentifier());
                    // publish message
                    Message<OfflineMessage> msg = HueMessageFactory.newOfflineMessage(this.bridgeAddress, light);
                    tryPublish(msg);
                } else if (!lastState.isReachable() && currentState.isReachable()) {
                    logger.debug("Device online Light {} hue:{}", light.getIdentifier(), currentState.getHue());
                    // publish message
                    Message<OnlineMessage> msg = HueMessageFactory.newOnlineMessage(this.bridgeAddress, light);
                    tryPublish(msg);
                } else if (lastState.isReachable() && currentState.isReachable() && !lastState.equals(currentState)) {
                    if (!lastState.isOn() && currentState.isOn()) {
                        logger.debug("Device turned on Light {} hue:{}", light.getIdentifier(), currentState.getHue());
                        Message<TriggerMessage> msg = HueMessageFactory.newTriggerMessage(this.bridgeAddress, light, Description.TRIGGER_ID_TURN_ON);
                        tryPublish(msg);
                    } else if (lastState.isOn() && !currentState.isOn()) {
                        logger.debug("Device turned off Light {}", light.getIdentifier());
                        Message<TriggerMessage> msg = HueMessageFactory.newTriggerMessage(this.bridgeAddress, light, Description.TRIGGER_ID_TURN_OFF);
                        tryPublish(msg);
                    } else if (lastState.isOn() && currentState.isOn()) {
                        logger.debug("Device state changed Light {} hue:{}", light.getIdentifier(), currentState.getHue());
                        Message<TriggerMessage> msg = HueMessageFactory.newTriggerMessage(this.bridgeAddress, light, Description.TRIGGER_ID_STATE_CHANGED);
                        msg.setQos(QoS.MOST_ONCE);  // state change event is not that important
                        tryPublish(msg);
                    }
                }
                newStates.put(light.getIdentifier(), currentState);
            }

            this.states = newStates;
        }
    }

    @Override
    public void onBridgeConnected(PHBridge phBridge) {
        logger.trace("Received onBridgeConnected event");

        // enable heartbeat
        this.bridge = phBridge;
        this.hb.enableLightsHeartbeat(this.bridge, PHHueSDK.HB_INTERVAL);

        // replace with current states
        this.states.clear();
        List<PHLight> lights = this.bridge.getResourceCache().getAllLights();
        for (PHLight light : lights) {
            PHLightState lightState = light.getLastKnownLightState();
            // save id : state mapping
            this.states.put(light.getIdentifier(), lightState);
            // publish message if light is online
            if (lightState.isReachable()) {
                logger.debug("Device online Light {} hue:{}", light.getIdentifier(), lightState.getHue());
                Message<OnlineMessage> msg = HueMessageFactory.newOnlineMessage(this.bridgeAddress, light);
                tryPublish(msg);
            }
        }
    }

    @Override
    public void onAuthenticationRequired(PHAccessPoint phAccessPoint) {
        logger.trace("Received onAuthenticationRequired event from {}", phAccessPoint.getIpAddress());
    }

    @Override
    public void onAccessPointsFound(List<PHAccessPoint> bridgeAddress) {
        // logger.trace("Received onAccessPointsFound event from {} bridges", bridgeAddress.size());
        // Handle your bridge search results here.
        // Typically if multiple results are returned you will want to display them in a list and let the user select their bridge.
        // If one is found you may opt to connect automatically to that bridge.
        if (!bridgeAddress.isEmpty()) {
            PHAccessPoint newAddress = bridgeAddress.get(0);
            if (this.bridgeAddress == null
                    || !this.bridgeAddress.getIpAddress().equals(newAddress.getIpAddress())
                    || !this.bridgeAddress.getMacAddress().equals(newAddress.getMacAddress())) {
                this.bridgeAddress = newAddress;
                logger.debug("Found new Hue Bridge ip:{} mac:{}",
                        this.bridgeAddress.getIpAddress(), this.bridgeAddress.getMacAddress());
                this.bridgeAddress.setUsername(this.userName);
                // since new bridge is found, disconnect from old bridge
                if (this.bridge != null) {
                    this.hb.disableAllHeartbeats(this.bridge);
                    this.hue.disconnect(this.bridge);
                    this.bridge = null;
                }
            }
        }
        // always try to re-connect to the bridge
        if (!this.hue.isAccessPointConnected(this.bridgeAddress)) {
            logger.debug("Try to connect to the bridge {}", this.bridgeAddress.getIpAddress());
            this.hue.connect(this.bridgeAddress);
        }
    }

    @Override
    public void onError(int i, String s) {
        logger.trace("Received onError event: {} {}", i, s);
    }

    @Override
    public void onConnectionResumed(PHBridge phBridge) {
        // logger.trace("Received onConnectionResumed event");
    }

    @Override
    public void onConnectionLost(PHAccessPoint phAccessPoint) {
        logger.trace("Received onConnectionLost event from {}", phAccessPoint.getIpAddress());
    }

    @Override
    public void onParsingErrors(List<PHHueParsingError> list) {
        logger.trace("Received onParsingErrors event");
        for (PHHueParsingError error : list) {
            logger.trace("ParsingError: {}", error.getMessage());
        }
    }

    /**
     * Try to publish message to mqtt topic
     *
     * @param payload Payload
     */
    protected void tryPublish(Message payload) {
        try {
            // since MQTT broker is at localhost, always use QoS 1
            this.mqttListener.publish(QoS.LEAST_ONCE, payload);
        } catch (MqttException e) {
            logger.warn("MQTT publish exception: {}", ExceptionUtils.getMessage(e));
        } catch (JsonProcessingException e) {
            logger.error("Json process exception: {}", ExceptionUtils.getMessage(e));
        }
    }

    /**
     * Execute action based on ActionMessage
     *
     * @param message Message<ActionMessage>
     */
    public void doAction(Message<ActionMessage> message) {
        ActionMessage action = message.getPayload();
        Map<String, Object> attributes = action.getAttributes();

        if (!this.hue.isAccessPointConnected(this.bridgeAddress)) {
            logger.debug("Try to do {} action but not connect to bridge", action.getActionId());
            return;
        }

        // handle authentication action first
        if (action.getActionId().equals(Description.ACTION_ID_AUTHENTICATION)) {
            this.hue.startPushlinkAuthentication(this.bridgeAddress);
            return;
        }

        if (attributes == null || !attributes.containsKey("lightId")) {
            logger.warn("{} action does not contains hue attribute", action.getActionId());
            return;
        }
        String lightId = String.valueOf(attributes.get("lightId"));

        // prepare light state
        PHLightState lightState = new PHLightState();
        switch (action.getActionId()) {
            case Description.ACTION_ID_TURN_ON:
                if (!attributes.containsKey("hue")) {
                    logger.warn("{} action does not contains hue attribute", action.getActionId());
                    return;
                }
                lightState.setHue((int) attributes.get("hue"));
                break;
            case Description.ACTION_ID_TURN_OFF:
                lightState.setHue((int) attributes.get("hue"));
                break;
            case Description.ACTION_ID_CHANGE_STATE:
                if (!attributes.containsKey("hue")) {
                    logger.warn("{} action does not contains hue attribute", action.getActionId());
                    return;
                }
                lightState.setHue((int) attributes.get("hue"));
                break;
            default:
                logger.warn("Un-recognized action id {}", message.getPayload().getActionId());
                return;
        }

        // update light state
        this.bridge.updateLightState(lightId, lightState, new HueLightListener(this, lightId));
    }
}
