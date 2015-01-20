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
     * Get Hue Adapter's Device Id
     *
     * @return Hue Adapter's Device Id
     */
    public static String getAdapterId() {
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
     * Get Hue Bridge's Device Id
     *
     * @param mac Bride's Mac Address
     * @return Hue Bridge's Device Id
     */
    public static String getBridgeId(String mac) {
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
     * Light Id -> Device Id
     *
     * @param lightId Light Id from HueSDK
     * @return Device Id used in Evolution protocol
     */
    public static String fromLightId(String mac, String lightId) {
        return getBridgeId(mac) + "-" + lightId;
    }
}
