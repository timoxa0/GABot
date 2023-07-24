package ru.timoxa0.GABot.models;

import com.github.f4b6a3.uuid.UuidCreator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import ru.timoxa0.GABot.handlers.DBHandler;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MCUser {
    private final static Logger logger = LogManager.getLogger(MCUser.class);
    private final static DBHandler dbHandler = DBHandler.getDBHandler();
    private final String id;
    private final String uuid;
    private String password;
    private String name;

    public static MCUser getByID(String id) {
        return dbHandler.getUserByID(id);
    }
    public static MCUser getByName(String name) {
        return dbHandler.getUserByName(name);
    }

    public static boolean checkFor(String id) {
        return dbHandler.checkForUser(id);
    }

    public MCUser(String id, String name, String password, String uuid) {
        this.id = id;
        this.name = name;
        this.password = encodePassword(password);
        this.uuid = uuid;
    }

    public MCUser(String id, String name, String password) {
        this.id = id;
        this.name = name;
        this.password = encodePassword(password);
        this.uuid = String.valueOf(UuidCreator.getTimeBased());
    }

    public String getID() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getPassword() {
        return password;
    }

    public String getUUID() {
        return uuid;
    }

    public MCUser setName(String name) {
        this.name = name;
        return this;
    }

    public MCUser setPassword(String password) {
        this.password = encodePassword(password);
        return this;
    }

    public boolean commit() {
        boolean result = dbHandler.updateUser(this);
        if (result) { logger.info(String.format("Updated user [id:name]: %s:%s", this.id, this.name)); }
        return result;
    }

    @Override
    public String toString() {
        return String.format("ID: %s\nUUID: %s\nUsername: %s\nPassword: %s",
                this.id, this.uuid, this.name, this.password
        );
    }

    private String encodePassword(@NotNull String password) {
        String encoded_password = null;
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] sha_bytes = md.digest(password.getBytes(StandardCharsets.UTF_8));
            BigInteger number = new BigInteger(1, sha_bytes);
            StringBuilder sha_password = new StringBuilder(number.toString(16));
            while (sha_password.length() < 64) {
                sha_password.insert(0, '0');
            }
            encoded_password = sha_password.toString();
        } catch (NoSuchAlgorithmException e) {
            logger.trace(e.getMessage());
        }
        return encoded_password;
    }
}
