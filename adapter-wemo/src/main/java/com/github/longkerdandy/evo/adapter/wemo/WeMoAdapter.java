package com.github.longkerdandy.evo.adapter.wemo;

import com.github.longkerdandy.evo.adapter.wemo.upnp.WeMoRegistryListener;
import com.github.longkerdandy.evo.adapter.wemo.upnp.WeMoUpnpServiceConfiguration;
import org.fourthline.cling.UpnpService;
import org.fourthline.cling.UpnpServiceImpl;
import org.fourthline.cling.model.message.header.DeviceTypeHeader;
import org.fourthline.cling.model.meta.LocalDevice;
import org.fourthline.cling.model.meta.RemoteDevice;
import org.fourthline.cling.model.types.DeviceType;
import org.fourthline.cling.registry.Registry;
import org.fourthline.cling.registry.RegistryListener;

/**
 * Belkin WeMo adapter
 */
public class WeMoAdapter {

    public static void main(String[] args) throws Exception {
        // This will create necessary network resources for UPnP right away
        System.out.println("Starting Cling...");
        UpnpService upnpService = new UpnpServiceImpl(new WeMoUpnpServiceConfiguration());
        upnpService.getRegistry().addListener(new WeMoRegistryListener(upnpService));

        // Send a search message to belkin wemo devices, they should respond soon
        upnpService.getControlPoint().search(new DeviceTypeHeader(new DeviceType("Belkin", "controllee", 1)));  // switch
        upnpService.getControlPoint().search(new DeviceTypeHeader(new DeviceType("Belkin", "sensor", 1)));  // motion

        // Let's wait 10 seconds for them to respond
        System.out.println("Waiting 10 seconds before shutting down...");
        Thread.sleep(10000);

        // Release all resources and advertise BYEBYE to other UPnP devices
        System.out.println("Stopping Cling...");
        upnpService.shutdown();
    }
}
