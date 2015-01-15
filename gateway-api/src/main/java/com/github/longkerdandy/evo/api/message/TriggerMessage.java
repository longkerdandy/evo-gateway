package com.github.longkerdandy.evo.api.message;

import java.util.Map;

/**
 * Trigger
 * Device send certain type of notification(trigger) when state changed
 */
@SuppressWarnings("unused")
public class TriggerMessage {

    private String triggerId;               // Trigger Id
    private int overridePolicy;             // Attributes Override Policy
    private Map<String, Object> attributes; // Attributes

    public String getTriggerId() {
        return triggerId;
    }

    public void setTriggerId(String triggerId) {
        this.triggerId = triggerId;
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
