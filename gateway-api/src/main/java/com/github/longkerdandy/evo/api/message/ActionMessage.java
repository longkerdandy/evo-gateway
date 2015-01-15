package com.github.longkerdandy.evo.api.message;

import java.util.Map;

/**
 * Action
 * Device receives certain type of command(action) and changes its state
 */
@SuppressWarnings("unused")
public class ActionMessage {

    private String actionId;                // Action Id
    private int overridePolicy;             // Attributes Override Policy
    private Map<String, Object> attributes; // Attributes

    public String getActionId() {
        return actionId;
    }

    public void setActionId(String actionId) {
        this.actionId = actionId;
    }

    public int getOverridePolicy() {
        return overridePolicy;
    }

    public void setOverridePolicy(int overridePolicy) {
        this.overridePolicy = overridePolicy;
    }

    public Map<String, Object> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<String, Object> attributes) {
        this.attributes = attributes;
    }
}
