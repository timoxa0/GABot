package ru.timoxa0.GABot.handlers;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

public class TranslationHandler {
    private static TranslationHandler configHandler = null;
    private final Properties prop = new Properties();

    public static synchronized void createTranslationHandler(String trnName) throws IOException {
        configHandler = new TranslationHandler(trnName);
    }

    public static synchronized TranslationHandler getTranslationHandler() {
        assert configHandler != null;
        return configHandler;
    }

    private TranslationHandler(String trnName) throws IOException {
        try (InputStream langRes = TranslationHandler.class.getResourceAsStream(String.format("/assets/lang/%s.properties", trnName))) {
            assert langRes != null;
            prop.load(new InputStreamReader(langRes, StandardCharsets.UTF_8));
        }
    }

    public String getProperty(String property) {
        return prop.getProperty(property);
    }

    public String getProperty(String property, String defaultValue) {
        return prop.getProperty(property, defaultValue);
    }
}
