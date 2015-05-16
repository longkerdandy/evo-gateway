package com.github.longkerdandy.evo.adapter.wemo.upnp;

import org.junit.Test;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.IOException;

/**
 * WeMoUDA10ServiceDescriptorBinderSAXImpl Test
 */
public class WeMoUDA10ServiceDescriptorBinderSAXImplTest {

    private String serviceXml = "<?xml version=\"1.0\"?>\n" +
            "<scpd xmlns=\"urn:Belkin:service-1-0\">\n" +
            "\n" +
            "  <specVersion>\n" +
            "    <major>1</major>\n" +
            "    <minor>0</minor>\n" +
            "  </specVersion>\n" +
            "  \n" +
            "  <actionList>\n" +
            "    <action>\n" +
            "\t<name>GetDeviceInformation</name>\n" +
            "\t<argumentList>\n" +
            "\t    <argument>\n" +
            "\t\t<retval />\n" +
            "\t\t<name>DeviceInformation</name>\n" +
            "\t\t<relatedStateVariable>DeviceInformation</relatedStateVariable>\n" +
            "\t\t<direction>out</direction>\n" +
            "\t    </argument>\n" +
            "      <!--  Adding Countdown Time -->\n" +
            "      <argument>\n" +
            "        <retval />\n" +
            "        <name>CountdownTime</name>\n" +
            "        <relatedStateVariable>CountdownTime</relatedStateVariable>\n" +
            "        <direction>out</direction>\n" +
            "      </argument>\n" +
            "\t</argumentList>\n" +
            "    </action>\n" +
            "    <action>\n" +
            "\t<name>GetInformation</name>\n" +
            "\t<argumentList>\n" +
            "\t    <argument>\n" +
            "\t\t<retval />\n" +
            "\t\t<name>Information</name>\n" +
            "\t\t<relatedStateVariable>Information</relatedStateVariable>\n" +
            "\t\t<direction>out</direction>\n" +
            "\t    </argument>\n" +
            "\t</argumentList>\n" +
            "    </action>\n" +
            "    <action>\n" +
            "\t<name>OpenInstaAP</name>\n" +
            "\t<argumentList>\n" +
            "\t    <argument>\n" +
            "\t    </argument>\n" +
            "\t</argumentList>\n" +
            "    </action>\n" +
            "    <action>\n" +
            "\t<name>CloseInstaAP</name>\n" +
            "\t<argumentList>\n" +
            "\t    <argument>\n" +
            "\t    </argument>\n" +
            "\t</argumentList>\n" +
            "    </action>\n" +
            "    <action>\n" +
            "\t<name>GetConfigureState</name>\n" +
            "\t<argumentList>\n" +
            "\t    <argument>\n" +
            "\t\t<retval />\n" +
            "\t\t<name>ConfigureState</name>\n" +
            "\t\t<relatedStateVariable>ConfigureState</relatedStateVariable>\n" +
            "\t\t<direction>out</direction>\n" +
            "\t    </argument>\n" +
            "\t</argumentList>\n" +
            "    </action>\n" +
            "    <action>\n" +
            "      <name>InstaConnectHomeNetwork</name>    \n" +
            "      <argumentList>\n" +
            "         <argument>\n" +
            "           <retval />\n" +
            "           <name>ssid</name>\n" +
            "           <relatedStateVariable>ssid</relatedStateVariable>\n" +
            "           <direction>in</direction>\n" +
            "         </argument>\n" +
            "         <argument>\n" +
            "\t    <retval />\n" +
            "\t    <name>auth</name>\n" +
            "\t    <relatedStateVariable>auth</relatedStateVariable>\n" +
            "\t    <direction>in</direction>\n" +
            "        </argument>\n" +
            "        <argument>\n" +
            "\t    <retval />\n" +
            "            <name>password</name>\n" +
            "            <relatedStateVariable>password</relatedStateVariable>\n" +
            "            <direction>in</direction>\n" +
            "       </argument>\n" +
            "       <argument>\n" +
            "            <retval />\n" +
            "            <name>encrypt</name>\n" +
            "            <relatedStateVariable>encrypt</relatedStateVariable>\n" +
            "            <direction>in</direction>\n" +
            "       </argument>\n" +
            "       <argument>\n" +
            "            <retval />\n" +
            "            <name>channel</name>\n" +
            "            <relatedStateVariable>channel</relatedStateVariable>\n" +
            "            <direction>in</direction>\n" +
            "       </argument>\n" +
            "\t<argument>\n" +
            "\t    <retval />\n" +
            "\t    <name>brlist</name>\n" +
            "\t    <relatedStateVariable>brlist</relatedStateVariable>\n" +
            "\t    <direction>in</direction>\n" +
            "\t</argument>\n" +
            "      </argumentList>\n" +
            "    </action>\n" +
            "    <action>\n" +
            "\t<name>UpdateBridgeList</name>\n" +
            "\t<argumentList>\n" +
            "\t    <argument>\n" +
            "\t\t<retval />\n" +
            "\t\t<name>BridgeList</name>\n" +
            "\t\t<relatedStateVariable>BridgeList</relatedStateVariable>\n" +
            "\t\t<direction>in</direction>\n" +
            "\t    </argument>\n" +
            "\t</argumentList>\n" +
            "    </action>\n" +
            "    <action>\n" +
            "\t<name>GetRouterInformation</name>\n" +
            "\t<argumentList>\n" +
            "\t    <argument>\n" +
            "\t\t<retval />\n" +
            "\t\t<name>mac</name>\n" +
            "\t\t<relatedStateVariable>mac</relatedStateVariable>\n" +
            "\t\t<direction>out</direction>\n" +
            "\t    </argument>\n" +
            "         <argument>\n" +
            "           <retval />\n" +
            "           <name>ssid</name>\n" +
            "           <relatedStateVariable>ssid</relatedStateVariable>\n" +
            "           <direction>out</direction>\n" +
            "         </argument>\n" +
            "         <argument>\n" +
            "\t    <retval />\n" +
            "\t    <name>auth</name>\n" +
            "\t    <relatedStateVariable>auth</relatedStateVariable>\n" +
            "\t    <direction>out</direction>\n" +
            "        </argument>\n" +
            "        <argument>\n" +
            "\t    <retval />\n" +
            "            <name>password</name>\n" +
            "            <relatedStateVariable>password</relatedStateVariable>\n" +
            "            <direction>out</direction>\n" +
            "       </argument>\n" +
            "       <argument>\n" +
            "            <retval />\n" +
            "            <name>encrypt</name>\n" +
            "            <relatedStateVariable>encrypt</relatedStateVariable>\n" +
            "            <direction>out</direction>\n" +
            "       </argument>\n" +
            "       <argument>\n" +
            "            <retval />\n" +
            "            <name>channel</name>\n" +
            "            <relatedStateVariable>channel</relatedStateVariable>\n" +
            "            <direction>out</direction>\n" +
            "       </argument>\n" +
            "\t</argumentList>\n" +
            "    </action>\n" +
            "    <action>\n" +
            "    <name>InstaRemoteAccess</name>\n" +
            "    <argumentList>\n" +
            "    <argument>\n" +
            "    <retval />\n" +
            "    <name>DeviceId</name>\n" +
            "    <relatedStateVariable>DeviceId</relatedStateVariable>\n" +
            "    <direction>in</direction>\n" +
            "    </argument>\n" +
            "    <argument>\n" +
            "    <retval />\n" +
            "    <name>dst</name>\n" +
            "    <relatedStateVariable>dst</relatedStateVariable>\n" +
            "    <direction>in</direction>\n" +
            "    </argument>\n" +
            "    <argument>\n" +
            "    <retval />\n" +
            "    <name>HomeId</name>\n" +
            "    <relatedStateVariable>HomeId</relatedStateVariable>\n" +
            "    <direction>in</direction>\n" +
            "    </argument>\n" +
            "    <argument>\n" +
            "    <retval />\n" +
            "    <name>DeviceName</name>\n" +
            "    <relatedStateVariable>DeviceName</relatedStateVariable>\n" +
            "    <direction>in</direction>\n" +
            "    </argument>\n" +
            "    <argument>\n" +
            "    <retval />\n" +
            "    <name>MacAddr</name>\n" +
            "    <relatedStateVariable>MacAddr</relatedStateVariable>\n" +
            "    <direction>in</direction>\n" +
            "    </argument>\n" +
            "    <argument>\n" +
            "    <retval />\n" +
            "    <name>pluginprivateKey</name>\n" +
            "    <relatedStateVariable>pluginprivateKey</relatedStateVariable>\n" +
            "    <direction>in</direction>\n" +
            "    </argument>\n" +
            "    <argument>\n" +
            "    <retval />\n" +
            "    <name>smartprivateKey</name>\n" +
            "    <relatedStateVariable>smartprivateKey</relatedStateVariable>\n" +
            "    <direction>in</direction>\n" +
            "    </argument>\n" +
            "    <argument>\n" +
            "    <retval />\n" +
            "    <name>smartUniqueId</name>\n" +
            "    <relatedStateVariable>smartUniqueId</relatedStateVariable>\n" +
            "    <direction>in</direction>\n" +
            "    </argument>\t \t  \n" +
            "    <argument>\n" +
            "    <retval />\n" +
            "    <name>numSmartDev</name>\n" +
            "    <relatedStateVariable>numSmartDev</relatedStateVariable>\n" +
            "    <direction>in</direction>\n" +
            "    </argument>\t \t  \n" +
            "    </argumentList>\n" +
            "    </action>\n" +
            "</actionList>\n" +
            "\n" +
            "  <serviceStateTable>\n" +
            "  \n" +
            "    <stateVariable sendEvents=\"yes\">\n" +
            "      <name>DeviceInformation</name>\n" +
            "      <dataType>string</dataType>\n" +
            "      <defaultValue>0</defaultValue>\n" +
            "    </stateVariable>\n" +
            "\n" +
            "    <!--  Adding Countdown Time -->\n" +
            "    <stateVariable sendEvents=\"yes\">\n" +
            "      <name>CountdownTime</name>\n" +
            "      <dataType>string</dataType>\n" +
            "      <defaultValue>0</defaultValue>\n" +
            "    </stateVariable>\n" +
            "\n" +
            "    <stateVariable sendEvents=\"yes\">\n" +
            "      <name>Information</name>\n" +
            "      <dataType>string</dataType>\n" +
            "      <defaultValue>0</defaultValue>\n" +
            "    </stateVariable>\n" +
            "\n" +
            "    <stateVariable sendEvents=\"yes\">\n" +
            "      <name>ConfigureState</name>\n" +
            "      <dataType>string</dataType>\n" +
            "      <defaultValue>0</defaultValue>\n" +
            "    </stateVariable>\n" +
            "\n" +
            "    <stateVariable sendEvents=\"yes\">\n" +
            "      <name>BridgeList</name>\n" +
            "      <dataType>string</dataType>\n" +
            "      <defaultValue>0</defaultValue>\n" +
            "    </stateVariable>\n" +
            "\n" +
            "    <stateVariable sendEvents=\"yes\">\n" +
            "      <name>mac</name>\n" +
            "      <dataType>string</dataType>\n" +
            "      <defaultValue>0</defaultValue>\n" +
            "    </stateVariable>\n" +
            "\n" +
            "    <stateVariable sendEvents=\"yes\">\n" +
            "      <name>ssid</name>\n" +
            "      <dataType>string</dataType>\n" +
            "      <defaultValue>0</defaultValue>\n" +
            "    </stateVariable>\n" +
            "\n" +
            "    <stateVariable sendEvents=\"yes\">\n" +
            "      <name>auth</name>\n" +
            "      <dataType>string</dataType>\n" +
            "      <defaultValue>0</defaultValue>\n" +
            "    </stateVariable>\n" +
            "\n" +
            "    <stateVariable sendEvents=\"yes\">\n" +
            "      <name>password</name>\n" +
            "      <dataType>string</dataType>\n" +
            "      <defaultValue>0</defaultValue>\n" +
            "    </stateVariable>\n" +
            "\n" +
            "    <stateVariable sendEvents=\"yes\">\n" +
            "      <name>encrypt</name>\n" +
            "      <dataType>string</dataType>\n" +
            "      <defaultValue>0</defaultValue>\n" +
            "    </stateVariable>\n" +
            "\n" +
            "    <stateVariable sendEvents=\"yes\">\n" +
            "      <name>channel</name>\n" +
            "      <dataType>string</dataType>\n" +
            "      <defaultValue>0</defaultValue>\n" +
            "    </stateVariable>\n" +
            "\n" +
            "  <stateVariable sendEvents=\"yes\">\t\n" +
            "\t<name>PairingStatus</name>\n" +
            "      <dataType>string</dataType>\n" +
            "      <defaultValue>Connecting</defaultValue>\n" +
            "    </stateVariable>\n" +
            "\n" +
            "    <stateVariable sendEvents=\"yes\">\n" +
            "    <name>statusCode</name>\n" +
            "    <dataType>string</dataType>\n" +
            "    <defaultValue>0</defaultValue>\n" +
            "    </stateVariable>\n" +
            "\n" +
            "  </serviceStateTable>\n" +
            "  \n" +
            "  </scpd>\n";

    @Test
    public void fixDescriptorXmlTest() throws ParserConfigurationException, IOException, SAXException, TransformerException {
        WeMoUDA10ServiceDescriptorBinderSAXImpl binder = new WeMoUDA10ServiceDescriptorBinderSAXImpl();
        String xml = binder.fixDescriptorXml(serviceXml);
        assert !xml.contains("<argument></argument>");
    }
}
