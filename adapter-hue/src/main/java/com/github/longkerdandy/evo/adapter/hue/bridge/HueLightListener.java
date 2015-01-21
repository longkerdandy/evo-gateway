package com.github.longkerdandy.evo.adapter.hue.bridge;

import com.philips.lighting.hue.listener.PHLightListener;
import com.philips.lighting.model.PHBridgeResource;
import com.philips.lighting.model.PHHueError;
import com.philips.lighting.model.PHLight;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

/**
 * Hue Light Listener
 */
public class HueLightListener implements PHLightListener {

    // Logger
    private static final Logger logger = LoggerFactory.getLogger(HueLightListener.class);

    private final HueListener hueListener;  // Back reference to HueListener
    private final String lightId;           // Hue Light Id

    public HueLightListener(HueListener hueListener, String lightId) {
        this.hueListener = hueListener;
        this.lightId = lightId;
    }

    @Override
    public void onReceivingLightDetails(PHLight phLight) {
        logger.trace("Received onReceivingLightDetails event");
    }

    @Override
    public void onReceivingLights(List<PHBridgeResource> list) {
        logger.trace("Received onReceivingLights event");
    }

    @Override
    public void onSearchComplete() {
        logger.trace("Received onSearchComplete event");
    }

    @Override
    public void onSuccess() {
        logger.trace("Received onSuccess event");
    }

    @Override
    public void onError(int i, String s) {
        logger.trace("Received onError event");
    }

    @Override
    public void onStateUpdate(Map<String, String> map, List<PHHueError> list) {
        logger.trace("Received onStateUpdate event");
    }
}
