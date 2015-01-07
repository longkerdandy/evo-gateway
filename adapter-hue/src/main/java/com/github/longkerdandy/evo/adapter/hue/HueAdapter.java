package com.github.longkerdandy.evo.adapter.hue;

import com.fasterxml.uuid.EthernetAddress;
import com.fasterxml.uuid.Generators;
import com.fasterxml.uuid.impl.NameBasedGenerator;
import com.github.longkerdandy.evo.adapter.hue.bridge.HueListener;
import com.github.longkerdandy.evo.adapter.hue.bridge.HueSeeker;
import com.github.longkerdandy.evo.adapter.hue.prop.UserProperties;
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

    public static void main(String args[]) {
        // Gateway's Device Id
        EthernetAddress ea = EthernetAddress.fromInterface();
        if (ea == null) {
            logger.error("Can't determine ethernet address");
            System.exit(1);
        }
        NameBasedGenerator generator = Generators.nameBasedGenerator(NameBasedGenerator.NAMESPACE_URL);
        String gatewayId = generator.generate("https://github.com/longkerdandy/evo-gateway?mac=" + ea.toString()).toString();
        logger.trace("Gateway's device id is {}", gatewayId);

        // hue (sdk) instance
        PHHueSDK hue = PHHueSDK.getInstance();
        // load properties
        UserProperties.loadProperties();
        // register the PHSDKListener to receive callbacks from the bridge
        hue.getNotificationManager().registerSDKListener(new HueListener(gatewayId, hue));

        // starts a Hue Bridge Seeker thread
        // UPNP/Portal/IP search takes around 10 seconds
        // the PHSDKListener (onAccessPointsFound) will be notified with the bridges found
        PHBridgeSearchManager sm = (PHBridgeSearchManager) hue.getSDKService(PHHueSDK.SEARCH_BRIDGE);
        HueSeeker seeker = new HueSeeker(sm);
        Timer timer = new Timer(true);
        timer.scheduleAtFixedRate(seeker, 0, 30 * 1000); // 30s
    }
}
