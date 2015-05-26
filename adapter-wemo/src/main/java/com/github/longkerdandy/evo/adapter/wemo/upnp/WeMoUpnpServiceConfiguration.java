package com.github.longkerdandy.evo.adapter.wemo.upnp;

import org.fourthline.cling.DefaultUpnpServiceConfiguration;
import org.fourthline.cling.binding.xml.DeviceDescriptorBinder;
import org.fourthline.cling.binding.xml.ServiceDescriptorBinder;
import org.fourthline.cling.model.Namespace;
import org.fourthline.cling.model.types.ServiceType;
import org.fourthline.cling.transport.impl.AsyncServletStreamServerConfigurationImpl;
import org.fourthline.cling.transport.impl.AsyncServletStreamServerImpl;
import org.fourthline.cling.transport.impl.jetty.JettyServletContainer;
import org.fourthline.cling.transport.impl.jetty.StreamClientConfigurationImpl;
import org.fourthline.cling.transport.impl.jetty.StreamClientImpl;
import org.fourthline.cling.transport.spi.NetworkAddressFactory;
import org.fourthline.cling.transport.spi.StreamClient;
import org.fourthline.cling.transport.spi.StreamServer;


/**
 * UpnpServiceConfiguration for Belkin WeMo Devices
 */
public class WeMoUpnpServiceConfiguration extends DefaultUpnpServiceConfiguration {

    @Override
    public StreamClient createStreamClient() {
        return new StreamClientImpl(new StreamClientConfigurationImpl(getSyncProtocolExecutorService()));
    }

    @Override
    public StreamServer createStreamServer(NetworkAddressFactory networkAddressFactory) {
        return new AsyncServletStreamServerImpl(new AsyncServletStreamServerConfigurationImpl(JettyServletContainer.INSTANCE, networkAddressFactory.getStreamListenPort()));
    }

    /**
     * WeMo fixed device descriptor parser
     */
    @Override
    protected DeviceDescriptorBinder createDeviceDescriptorBinderUDA10() {
        return new WeMoRecoveringUDA10DeviceDescriptorBinderImpl();
    }

    /**
     * WeMo fixed service descriptor parser
     */
    @Override
    protected ServiceDescriptorBinder createServiceDescriptorBinderUDA10() {
        return new WeMoUDA10ServiceDescriptorBinderSAXImpl();
    }

    /**
     * GENA namespace
     */
    @Override
    protected Namespace createNamespace() {
        return new Namespace("/WeMo");
    }

    /**
     * Filter WeMo messages only
     */
    @Override
    public ServiceType[] getExclusiveServiceTypes() {
        return new ServiceType[]{new ServiceType("Belkin", "basicevent")};
    }
}
