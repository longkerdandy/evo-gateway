package com.github.longkerdandy.evo.adapter.wemo.upnp;

import org.fourthline.cling.model.message.IncomingDatagramMessage;
import org.fourthline.cling.model.message.UpnpHeaders;
import org.fourthline.cling.model.message.header.UpnpHeader;
import org.fourthline.cling.transport.impl.DatagramProcessorImpl;

import java.io.ByteArrayInputStream;
import java.net.DatagramPacket;
import java.net.InetAddress;

/**
 * Fixed DatagramProcessorImpl for WeMo devices
 */
public class WeMoDatagramProcessorImpl extends DatagramProcessorImpl {

    @Override
    @SuppressWarnings("unchecked")
    protected IncomingDatagramMessage readResponseMessage(InetAddress receivedOnAddress,
                                                          DatagramPacket datagram,
                                                          ByteArrayInputStream is,
                                                          int statusCode,
                                                          String statusMessage,
                                                          String httpProtocol) throws Exception {
        IncomingDatagramMessage msg = super.readResponseMessage(receivedOnAddress, datagram, is, statusCode, statusMessage, httpProtocol);

        // fix max-age to 10 minutes
        UpnpHeaders headers = msg.getHeaders();
        UpnpHeader maxAgeHeader = headers.getFirstHeader(UpnpHeader.Type.MAX_AGE);
        if (maxAgeHeader != null) {
            maxAgeHeader.setValue(600);
        }

        return msg;
    }
}
