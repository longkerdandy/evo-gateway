package com.github.longkerdandy.evo.adapter.wemo.upnp.scpd;

import java.util.List;

/**
 * SCPD
 */
@SuppressWarnings("unused")
public class ServiceScpd {

    private SpecVersion specVersion;
    private List<Action> actionList;
    private List<StateVariable> serviceStateTable;

    public ServiceScpd(SpecVersion specVersion, List<Action> actionList, List<StateVariable> serviceStateTable) {
        this.specVersion = specVersion;
        this.actionList = actionList;
        this.serviceStateTable = serviceStateTable;
    }

    public SpecVersion getSpecVersion() {
        return specVersion;
    }

    public void setSpecVersion(SpecVersion specVersion) {
        this.specVersion = specVersion;
    }

    public List<Action> getActionList() {
        return actionList;
    }

    public void setActionList(List<Action> actionList) {
        this.actionList = actionList;
    }

    public List<StateVariable> getServiceStateTable() {
        return serviceStateTable;
    }

    public void setServiceStateTable(List<StateVariable> serviceStateTable) {
        this.serviceStateTable = serviceStateTable;
    }
}
