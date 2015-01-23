package com.github.longkerdandy.evo.adapter.hue.message;

import com.github.longkerdandy.evo.adapter.hue.constant.Description;
import com.github.longkerdandy.evo.adapter.hue.constant.ID;
import com.github.longkerdandy.evo.api.message.*;
import com.github.longkerdandy.evo.api.protocol.OverridePolicy;
import com.philips.lighting.hue.sdk.PHAccessPoint;
import com.philips.lighting.model.PHLight;

import java.util.HashMap;
import java.util.Map;

/**
 * Message Factory for Hue Light/Bridge
 */
public class HueMessageFactory {

    /**
     * Create a new Message<OnlineMessage> for specific Hue Light
     *
     * @param bridgeAddress Hue Bridge Address
     * @param light         Hue Light
     * @return Message<OnlineMessage>
     */
    public static Message<OnlineMessage> newOnlineMessage(PHAccessPoint bridgeAddress, PHLight light) {
        // construct new message
        return MessageFactory.newOnlineMessage(ID.lightDeviceId(bridgeAddress.getMacAddress(), light.getIdentifier()),
                Description.ID,
                forgeAttributes(light));
    }

    /**
     * Create a new Message<OfflineMessage> for specific Hue Light
     *
     * @param bridgeAddress Hue Bridge Address
     * @param light         Hue Light
     * @return Message<OfflineMessage>
     */
    public static Message<OfflineMessage> newOfflineMessage(PHAccessPoint bridgeAddress, PHLight light) {
        // construct new message
        return MessageFactory.newOfflineMessage(ID.lightDeviceId(bridgeAddress.getMacAddress(), light.getIdentifier()));
    }

    /**
     * Create a new Message<TriggerMessage> for specific Hue Light
     *
     * @param bridgeAddress Hue Bridge Address
     * @param light         Hue Light
     * @return Message<TriggerMessage>
     */
    public static Message<TriggerMessage> newTriggerMessage(PHAccessPoint bridgeAddress, PHLight light, String triggerId) {
        // construct new message
        return MessageFactory.newTriggerMessage(ID.lightDeviceId(bridgeAddress.getMacAddress(), light.getIdentifier()),
                triggerId,
                OverridePolicy.REPLACE_TIMESTAMP,
                forgeAttributes(light));
    }

    /**
     * Forge/Construct necessary attributes for a Hue Light
     *
     * @param light Hue Light
     * @return Attributes
     */
    protected static Map<String, Object> forgeAttributes(PHLight light) {
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
        return attributes;
    }
}
