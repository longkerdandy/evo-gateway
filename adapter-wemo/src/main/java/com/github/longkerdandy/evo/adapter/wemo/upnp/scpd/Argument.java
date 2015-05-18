package com.github.longkerdandy.evo.adapter.wemo.upnp.scpd;

/**
 * SCPD
 */
@SuppressWarnings("unused")
public class Argument {

    private String name;
    private String relatedStateVariable;
    private String direction;

    public Argument(String name, String relatedStateVariable, String direction) {
        this.name = name;
        this.relatedStateVariable = relatedStateVariable;
        this.direction = direction;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRelatedStateVariable() {
        return relatedStateVariable;
    }

    public void setRelatedStateVariable(String relatedStateVariable) {
        this.relatedStateVariable = relatedStateVariable;
    }

    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }
}
