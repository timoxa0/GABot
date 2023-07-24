package ru.timoxa0.GABot.handlers;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Properties;

public class ConfigHandler {
    private static ConfigHandler configHandler = null;
    private final Properties prop = new Properties();

    public static synchronized void createConfigHandler(Path configPath) throws IOException {
        configHandler = new ConfigHandler(configPath);
    }

    public static synchronized ConfigHandler getConfigHandler() {
        assert configHandler != null;
        return configHandler;
    }

    public static synchronized ConfigHandler getConfigHandler(Path configPath) throws IOException {
        createConfigHandler(configPath);
        return configHandler;
    }

    private ConfigHandler(Path configPath) throws IOException {
        File configFile = new File(configPath.toUri());
        prop.load(new FileReader(configFile));
    }

    public String getProperty(String property) {
        return prop.getProperty(property);
    }
}
