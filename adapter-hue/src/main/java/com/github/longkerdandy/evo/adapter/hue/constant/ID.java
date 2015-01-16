package com.github.longkerdandy.evo.adapter.hue.constant;

import com.fasterxml.uuid.EthernetAddress;
import com.fasterxml.uuid.Generators;
import com.fasterxml.uuid.impl.NameBasedGenerator;
import com.github.longkerdandy.evo.api.util.UuidUtils;
import org.apache.commons.lang3.StringUtils;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * IDs
 */
public class ID {

    private static String gatewayId;    // Evolution Gateway's Device Id
    private static String bridgeMac;    // Hue Bridge's Mac Address
    private static String bridgeId;     // Hue Bridge's Device Id

    /**
     * Get Gateway's Device Id
     *
     * @return Gateway's Device Id
     * @throws IllegalStateException If can't determine ethernet address
     */
    public static String getGatewayId() {
        if (StringUtils.isNotEmpty(gatewayId)) {
            return gatewayId;
        }

        EthernetAddress ea = EthernetAddress.fromInterface();
        if (ea == null) {
            throw new IllegalStateException("Can't determine ethernet address");
        }
        NameBasedGenerator generator = Generators.nameBasedGenerator(NameBasedGenerator.NAMESPACE_URL);
        gatewayId = generator.generate("https://github.com/longkerdandy/evo-gateway?mac=" + ea.toString()).toString();
        return gatewayId;
    }

    /**
     * Get Hue Adapter's Device Id
     *
     * @return Hue Adapter's Device Id
     */
    public static String getAdapterId() {
        return getGatewayId() + "-hue";
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
