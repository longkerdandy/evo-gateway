package com.github.longkerdandy.evo.adapter.wemo.upnp.scpd;

/**
 * SCPD
 */
@SuppressWarnings("unused")
public class StateVariable {

    private String sendEvents;
    private String name;
    private String dataType;
    private String defaultValue;
    private AllowedValueList allowedValueList;
    private AllowedValueRange allowedValueRange;

    public StateVariable(String sendEvents, String name, String dataType, String defaultValue, AllowedValueList allowedValueList, AllowedValueRange allowedValueRange) {
        this.sendEvents = sendEvents;
        this.name = name;
        this.dataType = dataType;
        this.defaultValue = defaultValue;
        this.allowedValueList = allowedValueList;
        this.allowedValueRange = allowedValueRange;
    }

    public String getSendEvents() {
        return sendEvents;
    }

    public void setSendEvents(String sendEvents) {
        this.sendEvents = sendEvents;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public AllowedValueList getAllowedValueList() {
        return allowedValueList;
    }

    public void setAllowedValueList(AllowedValueList allowedValueList) {
        this.allowedValueList = allowedValueList;
    }

    public AllowedValueRange getAllowedValueRange() {
        return allowedValueRange;
    }

    public void setAllowedValueRange(AllowedValueRange allowedValueRange) {
        this.allowedValueRange = allowedValueRange;
    }
}
