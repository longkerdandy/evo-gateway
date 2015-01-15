package com.github.longkerdandy.evo.api.message;

import java.util.Map;

/**
 * Online Message
 * Must be sent before all other messages when device connecting to the Cloud
 */
@SuppressWarnings("unused")
public class OnlineMessage {

    private String pv;                      // Protocol Version
    private String descId;                  // Device Description (File) Id
    private String userId;                  // User ID (as controller)
    private String token;                   // Token
    private Map<String, Object> attributes; // Attributes

    public String getPv() {
        return pv;
    }

    public void setPv(String pv) {
        this.pv = pv;
    }

    public String getDescId() {
        return descId;
    }

    public void setDescId(String descId) {
        this.descId = descId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Map<String, Object> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<String, Object> attributes) {
        this.attributes = attributes;
    }
}
