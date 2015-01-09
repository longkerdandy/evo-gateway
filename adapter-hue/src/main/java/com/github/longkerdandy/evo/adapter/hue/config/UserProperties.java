package com.github.longkerdandy.evo.adapter.hue.config;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * User Properties
 * Save/Load user session data to the properties file.
 */
public class UserProperties {

    // Properties
    private static final Properties props = new Properties();
    // Properties File Name
    private static final String PROPS_FILE_NAME = "session.properties";

    /**
     * Get property
     */
    public static String getProperty(String key) {
        return props.getProperty(key);
    }

    /**
     * Set property
     */
    public static void setProperty(String key, String value) {
        props.setProperty(key, value);
    }

    /**
     * Load properties from file
     */
    public static void loadProperties() {
        try (FileInputStream in = new FileInputStream(PROPS_FILE_NAME)) {
            props.load(in);
        } catch (IOException e) {
            // Handle the IOException.
        }
    }

    /**
     * Save properties from file
     */
    public static void storeProperties() {
        try (FileOutputStream out = new FileOutputStream(PROPS_FILE_NAME)) {
            props.store(out, null);
        } catch (IOException e) {
            // Handle the IOException.
        }
    }
}
