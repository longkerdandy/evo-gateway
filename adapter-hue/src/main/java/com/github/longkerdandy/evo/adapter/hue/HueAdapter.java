package com.github.longkerdandy.evo.adapter.hue;

import com.github.longkerdandy.evo.adapter.hue.bridge.HueListener;
import com.github.longkerdandy.evo.adapter.hue.bridge.HueSeeker;
import com.github.longkerdandy.evo.adapter.hue.constant.ID;
import com.github.longkerdandy.evo.adapter.hue.mqtt.MqttGuard;
import com.github.longkerdandy.evo.adapter.hue.mqtt.MqttListener;
import com.philips.lighting.hue.sdk.PHBridgeSearchManager;
import com.philips.lighting.hue.sdk.PHHueSDK;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Timer;

/**
 * Philips Hue Adapter
 */
public class HueAdapter {

    // Logger
    private static final Logger logger = LoggerFactory.getLogger(HueAdapter.class);

    public static void main(String args[]) throws Exception {
        // register mqtt listener to the broker
        MqttListener mqttListener = new MqttListener("tcp://localhost:1883", ID.getAdapterId());
        logger.debug("The mqtt listener started");

        // starts a MQTT Listener Guard thread
        // try to re-connect to the mqtt broker if connection lost
        MqttGuard guard = new MqttGuard(mqttListener);
        Timer timerMqtt = new Timer(true);
        timerMqtt.scheduleAtFixedRate(guard, 0, 30 * 1000); // 30s
        logger.debug("The mqtt listener guard started");

        // hue (sdk) instance
        PHHueSDK hue = PHHueSDK.getInstance();
        // register the PHSDKListener to receive callbacks from the bridge
        HueListener hueListener = new HueListener(ID.getAdapterId(), hue, mqttListener);
        hue.getNotificationManager().registerSDKListener(hueListener);
        logger.debug("The hue listener started");

        // starts a Hue Bridge Seeker thread
        // UPNP/Portal/IP search takes around 10 seconds
        // the PHSDKListener (onAccessPointsFound) will be notified with the bridges found
        PHBridgeSearchManager sm = (PHBridgeSearchManager) hue.getSDKService(PHHueSDK.SEARCH_BRIDGE);
        HueSeeker seeker = new HueSeeker(sm);
        Timer timerHue = new Timer(true);
        timerHue.scheduleAtFixedRate(seeker, 0, 30 * 1000); // 30s
        logger.debug("The hue seeker started");

        // set back mqtt listener's reference
        mqttListener.setHueListener(hueListener);
        logger.debug("The hue adapter successfully started");
    }
}
