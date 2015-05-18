package com.github.longkerdandy.evo.adapter.wemo.upnp;

import com.github.longkerdandy.evo.adapter.wemo.upnp.scpd.*;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.StaxDriver;
import org.fourthline.cling.binding.xml.DescriptorBindingException;
import org.fourthline.cling.binding.xml.UDA10ServiceDescriptorBinderSAXImpl;
import org.fourthline.cling.model.ValidationException;
import org.fourthline.cling.model.meta.Service;

import java.util.Iterator;

/**
 * Fixed UDA10ServiceDescriptorBinderSAXImpl for Belkin WeMo Devices
 */
public class WeMoUDA10ServiceDescriptorBinderSAXImpl extends UDA10ServiceDescriptorBinderSAXImpl {

    private XStream xstream;

    public WeMoUDA10ServiceDescriptorBinderSAXImpl() {
        super();
        // init xstream
        this.xstream = new XStream(new StaxDriver());
        this.xstream.alias("scpd", ServiceScpd.class);
        this.xstream.alias("specVersion", SpecVersion.class);
        this.xstream.alias("action", Action.class);
        this.xstream.alias("argument", Argument.class);
        this.xstream.alias("stateVariable", StateVariable.class);
        this.xstream.useAttributeFor(StateVariable.class, "sendEvents");
        this.xstream.alias("allowedValueList", AllowedValueList.class);
        this.xstream.addImplicitCollection(AllowedValueList.class, "allowedValues", "allowedValue", String.class);
        this.xstream.alias("allowedValueRange", AllowedValueRange.class);
    }

    @Override
    public <S extends Service> S describe(S undescribedService, String descriptorXml) throws DescriptorBindingException, ValidationException {
        if (descriptorXml == null || descriptorXml.length() == 0) {
            throw new DescriptorBindingException("Null or empty descriptor");
        }

        // fix bad format argumentList
        descriptorXml = descriptorXml.replaceAll("<argumentList>\\s*<retval\\s*/>", "<argumentList>\n<argument>\n<retval/>");
        descriptorXml = descriptorXml.replaceAll("</direction>\\s*</argumentList>", "</direction>\n</argument>\n</argumentList>");
        // remove empty argument
        descriptorXml = descriptorXml.replaceAll("<argument>\\s*</argument>", "");
        // remove all <retval/>
        descriptorXml = descriptorXml.replaceAll("<retval\\s*/>", "");
        // add missing stateVariable & remove invalid stateVariable
        descriptorXml = fixStateVariable(descriptorXml);

        return super.describe(undescribedService, descriptorXml);
    }

    protected String fixStateVariable(String descriptorXml) {
        // xml -> object
        ServiceScpd serviceScpd = (ServiceScpd) this.xstream.fromXML(descriptorXml);
        // add missing StateVariable
        for (Action action : serviceScpd.getActionList()) {
            if (action.getArgumentList() == null) continue;
            action.getArgumentList().stream()
                    .filter(argument -> !containStateVariable(serviceScpd, argument.getRelatedStateVariable()))
                    .forEach(argument -> serviceScpd.getServiceStateTable()
                            .add(new StateVariable("no", argument.getRelatedStateVariable(), "string", null, null, null)));
        }
        // remove invalid stateVariable
        for (Iterator<StateVariable> iterator = serviceScpd.getServiceStateTable().iterator(); iterator.hasNext(); ) {
            StateVariable stateVariable = iterator.next();
            if (stateVariable.getName().contains("\"")) {
                iterator.remove();
            }
        }
        // object -> xml
        return this.xstream.toXML(serviceScpd);
    }

    protected boolean containStateVariable(ServiceScpd serviceScpd, String name) {
        for (StateVariable stateVariable : serviceScpd.getServiceStateTable()) {
            if (stateVariable.getName().equals(name)) {
                return true;
            }
        }
        return false;
    }
}
