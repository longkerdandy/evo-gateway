package com.github.longkerdandy.evo.adapter.wemo.upnp;

import org.fourthline.cling.DefaultUpnpServiceConfiguration;
import org.fourthline.cling.binding.xml.DeviceDescriptorBinder;
import org.fourthline.cling.binding.xml.ServiceDescriptorBinder;
import org.fourthline.cling.model.Namespace;

/**
 * UpnpServiceConfiguration for Belkin WeMo Devices
 */
public class WeMoUpnpServiceConfiguration extends DefaultUpnpServiceConfiguration {

    @Override
    protected DeviceDescriptorBinder createDeviceDescriptorBinderUDA10() {
        return new WeMoRecoveringUDA10DeviceDescriptorBinderImpl();
    }

    @Override
    protected ServiceDescriptorBinder createServiceDescriptorBinderUDA10() {
        return new WeMoUDA10ServiceDescriptorBinderSAXImpl();
    }

    @Override
    protected Namespace createNamespace() {
        return new Namespace("WeMo");
    }
}
