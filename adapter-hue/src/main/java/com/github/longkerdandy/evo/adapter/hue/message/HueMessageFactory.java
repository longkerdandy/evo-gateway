package com.github.longkerdandy.evo.adapter.hue.message;

import com.github.longkerdandy.evo.adapter.hue.constant.Description;
import com.github.longkerdandy.evo.adapter.hue.constant.ID;
import com.github.longkerdandy.evo.api.message.ConnectMessage;
import com.github.longkerdandy.evo.api.message.DisconnectMessage;
import com.github.longkerdandy.evo.api.message.Message;
import com.github.longkerdandy.evo.api.message.MessageFactory;
import com.philips.lighting.model.PHLight;

import java.util.HashMap;
import java.util.Map;

/**
 * Message Factory for Hue Light/Bridge
 */
public class HueMessageFactory {

    /**
     * Create a new Message<ConnectMessage> for specific Hue Light
     *
     * @param light Hue Light
     * @return Message<ConnectMessage>
     */
    public static Message<ConnectMessage> newConnectMessage(PHLight light) {
        // construct attributes
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("id", light.getIdentifier());
        attributes.put("model", light.getModelNumber());
        attributes.put("firmware", light.getVersionNumber());
        attributes.put("on", light.getLastKnownLightState().isOn());
        attributes.put("bright", light.getLastKnownLightState().getBrightness());
        attributes.put("hue", light.getLastKnownLightState().getHue());
        attributes.put("sat", light.getLastKnownLightState().getSaturation());
        attributes.put("x", light.getLastKnownLightState().getX());
        attributes.put("y", light.getLastKnownLightState().getY());
        attributes.put("ct", light.getLastKnownLightState().getCt());
        attributes.put("effect", light.getLastKnownLightState().getEffectMode());
        attributes.put("color", light.getLastKnownLightState().getColorMode());

        // construct new message
        return MessageFactory.newConnectMessage(ID.fromLightId(light.getIdentifier()), Description.ID, attributes);
    }

    /**
     * Create a new Message<DisconnectMessage> for specific Hue Light
     *
     * @param light Hue Light
     * @return Message<DisconnectMessage>
     */
    public static Message<DisconnectMessage> newDisconnectMessage(PHLight light) {
        // construct new message
        return MessageFactory.newDisconnectMessage(ID.fromLightId(light.getIdentifier()));
    }
}
