package ru.timoxa0.GABot.handlers;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Properties;

public class TranslationHandler {
    private static TranslationHandler configHandler = null;
    private final Properties prop = new Properties();

    public static synchronized void createTranslationHandler(Path configPath) throws IOException {
        configHandler = new TranslationHandler(configPath);
    }

    public static synchronized TranslationHandler getTranslationHandler() {
        assert configHandler != null;
        return configHandler;
    }

    private TranslationHandler(Path configPath) throws IOException {
        File configFile = new File(configPath.toUri());
        prop.load(new FileReader(configFile));
    }

    public String getProperty(String property) {
        return prop.getProperty(property);
    }

    public String getProperty(String property, String defaultValue) {
        return prop.getProperty(property, defaultValue);
    }
}
