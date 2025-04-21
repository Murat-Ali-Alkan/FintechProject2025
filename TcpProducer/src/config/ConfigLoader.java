package config;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Utility class for loading application configuration properties from {@code "config.properties"}.
 *
 * <p>This class reads key-value pairs from {@code config.properties}
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

    static {
        try (InputStream input = new FileInputStream("config.properties")) {
            config.load(input);
        } catch (IOException e) {
            throw new RuntimeException("Configuration file can't be loaded!", e);
        }
    }

    /**
     * Checks whether a key exists in the {@code config.properties}
     *
     * @param key the configuration key to look up
     * @return true or false depending on the keys existence
     */
    public static boolean checkKey(String key) {
        return config.containsKey(key);
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

    /**
     * Returns the {@code int} value of the specified configuration key, or the given default value
     * if the key does not exist.
     *
     * @param key the configuration key to look up
     * @param defaultValue the value to return if the key is not found
     * @return the {@code int} value for the key, or {@code defaultValue} if not found
     */
    public static int getIntProperty(String key, int defaultValue) {
        return Integer.parseInt(config.getProperty(key, String.valueOf(defaultValue)));
    }
}
