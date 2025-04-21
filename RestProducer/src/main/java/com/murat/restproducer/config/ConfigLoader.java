package com.murat.restproducer.config;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Utility class for loading application configuration properties from {@code "config.properties"}.
 *
 * <p>This class reads key-value pairs from {@code src/main/resources/config.properties}
 * when the class is first loaded. It provides a static method to retrieve configuration
 * values.</p>
 *
 * <p>If the file cannot be loaded, a {@link RuntimeException} is thrown at class initialization time.</p>
 *
 */
public class ConfigLoader {

    /**
     * Holds the loaded configuration properties.
     */
    private static final Properties config = new Properties();

    // Static initializer (Constructor) block to load properties at class load time
    static {
        try (InputStream input = new FileInputStream("src/main/resources/config.properties")) {
            config.load(input);
        } catch (IOException e) {
            throw new RuntimeException("Configuration file can't be loaded!", e);
        }
    }

    /**
     * Returns the value of the specified configuration key, or the given default value
     * if the key does not exist.
     *
     * @param key the configuration key to look up
     * @param defaultValue the value to return if the key is not found
     * @return the configuration value for the key, or {@code defaultValue} if not found
     */
    public static String getProperty(String key, String defaultValue) {
        return config.getProperty(key, defaultValue);
    }
}
