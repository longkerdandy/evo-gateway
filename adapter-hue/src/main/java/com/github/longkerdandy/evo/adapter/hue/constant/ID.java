package com.github.longkerdandy.evo.adapter.hue.constant;

import com.fasterxml.uuid.EthernetAddress;
import com.github.longkerdandy.evo.api.util.UuidUtils;
import org.apache.commons.lang3.StringUtils;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * IDs
 */
public class ID {

    private static String adapterId;    // Adapter Id
    private static String bridgeMac;    // Hue Bridge's Mac Address
    private static String bridgeId;     // Hue Bridge's Device Id

    /**
     * Generate Hue Adapter's Device Id
     *
     * @return Hue Adapter's Device Id
     */
    public static String adapterDeviceId() {
        if (StringUtils.isNotEmpty(adapterId)) {
            return adapterId;
        }

        // get local hardware(mac) address
        EthernetAddress ea = EthernetAddress.fromInterface();
        if (ea == null) {
            throw new IllegalStateException("Can't determine ethernet address");
        }

        try {
            URI uri = new URI("https://github.com/longkerdandy/evo-gateway/hue-adapter?mac=" + ea.toString());
            adapterId = UuidUtils.shortUuid(uri);
        } catch (URISyntaxException ignore) {
            // never happens
        }

        return adapterId;
    }

    /**
     * Generate Hue Bridge's Device Id
     *
     * @param mac Bride's Mac Address
     * @return Hue Bridge's Device Id
     */
    public static String bridgeDeviceId(String mac) {
        if (bridgeMac != null && bridgeMac.equals(mac)) {
            return bridgeId;
        }

        try {
            URI uri = new URI("http://www.meethue.com/bridge?mac=" + mac);
            bridgeMac = mac;
            bridgeId = UuidUtils.shortUuid(uri);
        } catch (URISyntaxException ignore) {
            // never happens
        }

        return bridgeId;
    }

    /**
     * Generate Hue Light's Device Id
     *
     * @param lightId Light Id from HueSDK (like 1, 2, 3)
     * @return Device Id used in Evolution protocol
     */
    public static String lightDeviceId(String mac, String lightId) {
        return bridgeDeviceId(mac) + "-" + lightId;
    }
}
