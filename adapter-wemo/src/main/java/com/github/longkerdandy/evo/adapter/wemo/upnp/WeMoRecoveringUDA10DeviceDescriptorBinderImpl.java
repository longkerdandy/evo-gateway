package com.github.longkerdandy.evo.adapter.wemo.upnp;

import org.fourthline.cling.binding.xml.DescriptorBindingException;
import org.fourthline.cling.binding.xml.RecoveringUDA10DeviceDescriptorBinderImpl;
import org.fourthline.cling.model.ValidationException;
import org.fourthline.cling.model.meta.Device;

/**
 * WeMo Fix
 */
public class WeMoRecoveringUDA10DeviceDescriptorBinderImpl extends RecoveringUDA10DeviceDescriptorBinderImpl {

    @Override
    public <D extends Device> D describe(D undescribedDevice, String descriptorXml) throws DescriptorBindingException, ValidationException {
        // Fix WeMo descriptor xml error
        // fix mime type
        descriptorXml = descriptorXml.replaceAll("<mimetype>jpg</mimetype>", "<mimetype>image/jpeg</mimetype>");

        return super.describe(undescribedDevice, descriptorXml);
    }
}
