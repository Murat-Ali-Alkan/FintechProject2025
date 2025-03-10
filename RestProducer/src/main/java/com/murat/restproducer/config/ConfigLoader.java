package com.murat.restproducer.config;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ConfigLoader {
    private static final Properties config = new Properties();

    static {
        try (InputStream input = new FileInputStream("src/main/resources/config.properties")) {
            config.load(input);
        } catch (IOException e) {
            throw new RuntimeException("Konfigürasyon dosyası yüklenemedi!", e);
        }
    }

    public static String getProperty(String key, String defaultValue) {
        return config.getProperty(key, defaultValue);
    }
}
