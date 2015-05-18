package com.github.longkerdandy.evo.adapter.wemo.upnp.scpd;

import java.util.List;

/**
 * SCPD
 */
@SuppressWarnings("unused")
public class Action {

    private String name;
    private List<Argument> argumentList;

    public Action(String name, List<Argument> argumentList) {
        this.name = name;
        this.argumentList = argumentList;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Argument> getArgumentList() {
        return argumentList;
    }

    public void setArgumentList(List<Argument> argumentList) {
        this.argumentList = argumentList;
    }
}
