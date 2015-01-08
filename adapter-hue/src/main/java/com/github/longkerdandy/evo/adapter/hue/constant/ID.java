package com.github.longkerdandy.evo.adapter.hue.constant;

import com.fasterxml.uuid.EthernetAddress;
import com.fasterxml.uuid.Generators;
import com.fasterxml.uuid.impl.NameBasedGenerator;
import org.apache.commons.lang3.StringUtils;

/**
 * IDs
 */
public class ID {

    private static String gatewayId;

    /**
     * Get Gateway's Device Id
     *
     * @return Gateway's Device Id
     * @throws Exception If can't determine ethernet address
     */
    public static String getGatewayId() throws Exception {
        if (StringUtils.isNotEmpty(gatewayId)) {
            return gatewayId;
        }

        EthernetAddress ea = EthernetAddress.fromInterface();
        if (ea == null) {
            throw new Exception("Can't determine ethernet address");
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
        return gatewayId + "-hue";
    }

    /**
     * Light Id -> Device Id
     *
     * @param lightId Light Id from HueSDK
     * @return Device Id used in Evolution protocol
     */
    public static String fromLightId(String lightId) {
        return gatewayId + "-hue-" + lightId;
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
