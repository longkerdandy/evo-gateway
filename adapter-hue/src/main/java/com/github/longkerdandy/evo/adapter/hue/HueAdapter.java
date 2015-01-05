package com.github.longkerdandy.evo.adapter.hue;

import com.github.longkerdandy.evo.adapter.hue.listener.HueListener;
import com.github.longkerdandy.evo.adapter.hue.prop.UserProperties;
import com.philips.lighting.hue.sdk.PHHueSDK;

/**
 * Philips Hue Adapter
 */
public class HueAdapter {

    public static void main(String args[]) {
        // hue (sdk) instance
        PHHueSDK hue = PHHueSDK.getInstance();
        // load properties
        UserProperties.loadProperties();
        // register the PHSDKListener to receive callbacks from the bridge
        hue.getNotificationManager().registerSDKListener(new HueListener());
    }
}
