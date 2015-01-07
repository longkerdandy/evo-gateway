package com.github.longkerdandy.evo.adapter.hue.bridge;

import com.philips.lighting.hue.sdk.PHBridgeSearchManager;

import java.util.TimerTask;

/**
 * Hue Bridge Seeker
 */
public class HueSeeker extends TimerTask {

    private PHBridgeSearchManager sm; // Search Manager from Hue SDK

    public HueSeeker(PHBridgeSearchManager sm) {
        this.sm = sm;
    }

    @Override
    public void run() {
        // Parameters:
        //      searchUpnp - indicates whether UPnP should be used for searching.
        //      searchPortal - indicates whether portal based discovery should be used for searching.
        //      searchIpAddress - indicates whether ip address based discovery should be used for searching.
        sm.search(true, false, true);
    }
}
