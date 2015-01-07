package com.github.longkerdandy.evo.adapter.hue.bridge;

import com.philips.lighting.hue.sdk.PHAccessPoint;
import com.philips.lighting.hue.sdk.PHHueSDK;
import com.philips.lighting.hue.sdk.PHSDKListener;
import com.philips.lighting.model.PHBridge;
import com.philips.lighting.model.PHHueParsingError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * PHSDKListener implementation
 */
public class HueListener implements PHSDKListener {

    // Logger
    private static final Logger logger = LoggerFactory.getLogger(HueListener.class);

    private final String userName;      // User Name, should be Gateway's Device Id
    private final PHHueSDK hue;         // HueSDK instance
    private PHAccessPoint bridge;       // Cached Hue Bridge Address
    private boolean isConnected;        // Is connected to the bridge

    public HueListener(String userName, PHHueSDK hue) {
        this.userName = userName;
        this.hue = hue;
    }

    @Override
    public void onCacheUpdated(List<Integer> list, PHBridge phBridge) {
        logger.trace("Received onCacheUpdated event");
    }

    @Override
    public void onBridgeConnected(PHBridge phBridge) {
        logger.trace("Received onBridgeConnected event");
        this.isConnected = true;
    }

    @Override
    public void onAuthenticationRequired(PHAccessPoint phAccessPoint) {
        logger.trace("Received onAuthenticationRequired event");
    }

    @Override
    public void onAccessPointsFound(List<PHAccessPoint> list) {
        logger.trace("Received onAccessPointsFound event");
        // Handle your bridge search results here.  Typically if multiple results are returned you will want to display them in a list
        // and let the user select their bridge.   If one is found you may opt to connect automatically to that bridge.
        if (!list.isEmpty()) {
            PHAccessPoint newBridge = list.get(0);
            if (this.bridge == null
                    || !this.bridge.getIpAddress().equals(newBridge.getIpAddress())
                    || !this.bridge.getMacAddress().equals(newBridge.getMacAddress())) {
                this.bridge = list.get(0);
                this.bridge.setUsername(this.userName);
                logger.debug("Found new Hue Bridge ip:{} mac:{} username:{}, will try to connect",
                        this.bridge.getIpAddress(), this.bridge.getMacAddress(), this.bridge.getUsername());

            }
        }
        // always try to re-connect to the bridge
        if (!isConnected) {
            logger.debug("Try to connect to the bridge {}", this.bridge.getIpAddress());
            this.hue.connect(this.bridge);
        }
    }

    @Override
    public void onError(int i, String s) {
        logger.trace("Received onError event");
    }

    @Override
    public void onConnectionResumed(PHBridge phBridge) {
        logger.trace("Received onConnectionResumed event");
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
}
