package com.example.parsertest.configuration;

import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.configuration2.ex.ConfigurationException;

import java.net.URI;
import java.net.URISyntaxException;

public class ConfigurationLoader {
    private static final Configuration configuration;
    static {
        Configurations configs = new Configurations();
        try {
            configuration = configs.properties("config.properties");
            setSystemProperties();
        } catch (ConfigurationException e) {
            throw new IllegalStateException("Failed to load configuration file", e);
        }
    }

    private ConfigurationLoader() {
        throw new IllegalStateException("Utility class, cannot instantiate");

    }

    public static final URI EPICHERCHELL_URI = initUri();

    private static void setSystemProperties() {
        configuration.getKeys().forEachRemaining(key -> {
            String value = configuration.getString(key);
            System.setProperty(key, value);
        });
    }

    private static URI initUri() {
        try {
            return new URI(configuration.getString("epicherchel.url"));
        } catch (URISyntaxException e) {
            throw new IllegalStateException("Invalid URI in configuration file", e);
        }
    }

    public static String getProperty(String key) {
        if (key == null)
            return null;
        return configuration.getString(key);
    }
}
