package com.github.longkerdandy.evo.api.protocol;

/**
 * Override Policy
 */
@SuppressWarnings("unused")
public class OverridePolicy {

    public static final int REPLACE_FORCE = 1;                      // Replace all attributes (old attributes will be dropped)
    public static final int REPLACE_TIMESTAMP = 2;                  // Replace all attributes only if the timestamp is newer (old attributes will be dropped)
    public static final int MERGE_FORCE = 3;                        // Merge the attributes
    public static final int MERGE_TIMESTAMP = 4;                    // Merge the attributes only if the timestamp is newer
    public static final int MERGE_ATTRIBUTE_TIMESTAMP = 5;          // Merge the attributes only if the timestamp is newer
}
