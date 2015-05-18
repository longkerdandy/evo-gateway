package com.github.longkerdandy.evo.adapter.wemo.upnp.scpd;

import com.thoughtworks.xstream.annotations.XStreamImplicit;

import java.util.List;

/**
 * SCPD
 */
@SuppressWarnings("unused")
public class AllowedValueList {

    private List<String> allowedValues;

    public AllowedValueList(List<String> allowedValues) {
        this.allowedValues = allowedValues;
    }

    public List<String> getAllowedValues() {
        return allowedValues;
    }

    public void setAllowedValues(List<String> allowedValues) {
        this.allowedValues = allowedValues;
    }
}
