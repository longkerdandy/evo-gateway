package com.github.longkerdandy.evo.adapter.wemo.upnp;

import org.fourthline.cling.binding.xml.DescriptorBindingException;
import org.fourthline.cling.binding.xml.RecoveringUDA10DeviceDescriptorBinderImpl;
import org.fourthline.cling.model.ValidationException;
import org.fourthline.cling.model.meta.Device;

/**
 * Fixed RecoveringUDA10DeviceDescriptorBinderImpl for Belkin WeMo Devices
 */
public class WeMoRecoveringUDA10DeviceDescriptorBinderImpl extends RecoveringUDA10DeviceDescriptorBinderImpl {

    @Override
    public <D extends Device> D describe(D undescribedDevice, String descriptorXml) throws DescriptorBindingException, ValidationException {
        // fix xml namespace
        descriptorXml = descriptorXml.replaceAll("urn:Belkin:device-1-0", "urn:schemas-upnp-org:device-1-0");
        // fix mime type
        descriptorXml = descriptorXml.replaceAll("<mimetype>jpg</mimetype>", "<mimetype>image/jpeg</mimetype>");

        return super.describe(undescribedDevice, descriptorXml);
    }
}
