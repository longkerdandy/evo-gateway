package com.github.longkerdandy.evo.adapter.hue.protocol;

import com.github.longkerdandy.evo.adapter.hue.HueAdapter;

/**
 * Evolution Device Id
 */
public class DeviceId {

    /**
     * Light Id -> Device Id
     *
     * @param lightId Light Id from HueSDK
     * @return Device Id used in Evolution protocol
     */
    public static String fromLightId(String lightId) {
        return HueAdapter.gatewayId + "-hue-" + lightId;
    }

    /**
     * Device Id -> Light Id
     *
     * @param deviceId Light Id from HueSDK
     * @return Light Id from HueSDK
     */
    public static String toLightId(String deviceId) {
        int index = deviceId.lastIndexOf("-hue-");
        if (index >= 0 && deviceId.length() > index + 5) {
            return deviceId.substring(index + 5);
        } else {
            return null;
        }
    }
}
