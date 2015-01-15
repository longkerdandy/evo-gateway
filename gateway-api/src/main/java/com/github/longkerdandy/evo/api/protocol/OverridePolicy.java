package com.github.longkerdandy.evo.api.protocol;

/**
 * Override Policy
 */
@SuppressWarnings("unused")
public class OverridePolicy {

    public static final int REPLACE = 1;                // Replace the attributes
    public static final int REPLACE_ALL = 2;            // Replace all attributes (old attributes will be dropped)
    public static final int TIMESTAMP = 3;              // Replace the attributes only if the timestamp is newer
    public static final int TIMESTAMP_ALL = 4;          // Replace all attributes only if the timestamp is newer (old attributes will be dropped)
    public static final int ATTRIBUTE_TIMESTAMP = 5;    // Compare and Replace each attribute only if the timestamp is newer
}
