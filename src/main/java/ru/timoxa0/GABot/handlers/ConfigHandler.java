package ru.timoxa0.GABot.handlers;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.Properties;

public class ConfigHandler {
    private static final Logger logger = LogManager.getLogger(ConfigHandler.class);
    private static ConfigHandler configHandler = null;
    private final Properties prop = new Properties();
    private final File config;
    private final boolean isNew;

    private ConfigHandler(File config) throws IOException {
        this.config = config;
        if (validate()) {
            this.isNew = false;
            prop.load(new FileReader(config, StandardCharsets.UTF_8));
        } else {
            this.isNew = true;
            try (InputStream cfgRes = ConfigHandler.class.getResourceAsStream("/assets/sample.config")) {
                assert cfgRes != null;
                prop.load(new InputStreamReader(cfgRes, StandardCharsets.UTF_8));
                flush();
            }
        }
    }

    public static synchronized boolean createConfigHandler(File config) throws IOException {
        configHandler = new ConfigHandler(config);
        return !configHandler.isNew;
    }

    public static synchronized ConfigHandler getConfigHandler() {
        assert configHandler != null;
        return configHandler;
    }

    private boolean validate() throws IOException {
        boolean result = false;
        if (config.exists()) {
            Properties val_prop = new Properties();
            Properties sample_prop = new Properties();
            val_prop.load(new FileReader(config, StandardCharsets.UTF_8));
            sample_prop.load(new InputStreamReader(
                    Objects.requireNonNull(ConfigHandler.class.getResourceAsStream("/assets/sample.config")),
                    StandardCharsets.UTF_8
            ));
            result = val_prop.keySet().equals(sample_prop.keySet());
        }
        return result;
    }

    public void flush() {
        try {
            prop.store(new FileWriter(config, StandardCharsets.UTF_8), "GABot config file");
        } catch (IOException e) {
            logger.error("Failed to update config: " + e.getMessage());
        }
    }

    public boolean isNew() {
        return isNew;
    }

    public String getProperty(String property) {
        String r = prop.getProperty(property);
        if (r == null) {
            logger.warn("Failed to get property: " + property);
        }
        return r;
    }

    public String getProperty(String property, String defaultValue) {
        return prop.getProperty(property, defaultValue);
    }

    public void setProperty(String key, String value) {
        prop.setProperty(key, value);
    }
}
