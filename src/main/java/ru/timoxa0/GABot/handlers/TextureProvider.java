package ru.timoxa0.GABot.handlers;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.timoxa0.GABot.models.Cape;
import ru.timoxa0.GABot.models.MCUser;
import ru.timoxa0.GABot.models.Skin;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class TextureProvider {
    private static final Logger logger = LogManager.getLogger(TextureProvider.class);
    private static TextureProvider textureProvider;
    private final Path skinDir;
    private final Path capeDir;
    private final Path defaultSkin;
    private final Path defaultCape;

    private TextureProvider(Path skinDir, Path capeDir, Path defaultSkin, Path defaultCape) throws Throwable {
        this.skinDir = skinDir;
        this.capeDir = capeDir;
        this.defaultSkin = defaultSkin;
        this.defaultCape = defaultCape;
        if (!skinDir.toFile().exists()) {
            if (skinDir.toFile().mkdirs()) {
                logger.debug(String.format("Created skin directory: %s", skinDir.toAbsolutePath()));
            }
        }
        if (!capeDir.toFile().exists()) {
            if (capeDir.toFile().mkdirs()) {
                logger.debug(String.format("Created skin directory: %s", capeDir.toAbsolutePath()));
            }
        }
        if (!defaultSkin.toFile().exists()) {
            throw new Throwable(String.format("Default skin not found at %s", defaultSkin.toAbsolutePath()));
        }
        if (!defaultCape.toFile().exists()) {
            throw new Throwable(String.format("Default cape not found at %s", defaultSkin.toAbsolutePath()));
        }
    }

    public static synchronized TextureProvider getTextureHandler() {
        return textureProvider;
    }

    public static synchronized void createTextureHandler(
            Path skinDir, Path capeDir, Path defaultSkin, Path defaultCape
    ) throws Throwable {
        if (textureProvider == null) {
            textureProvider = new TextureProvider(skinDir, capeDir, defaultSkin, defaultCape);
        }
    }

    public Integer downloadSkin(MCUser user, InputStream inputStream) {
        try {
            BufferedImage img = ImageIO.read(inputStream);
            int width = img.getWidth();
            int height = img.getHeight();
            if (!((width % 64 == 0 && height % 64 == 0) && (width <= 512 && height <= 512))) {
                return 1;
            }
            Path path = this.skinDir.resolve(user.getUUID());
            File skin = new File(path.toUri());
            if (skin.createNewFile()) {
                logger.debug(String.format("Created skin file for [id:name]: %s:%s", user.getID(), user.getName()));
            }
            ImageIO.write(img, "png", skin);
            logger.info(String.format("Downloaded skin for [id:name]: %s:%s", user.getID(), user.getName()));
            return 0;
        } catch (IOException e) {
            logger.error(String.format("Failed to download skin for [id:name]: %s:%s", user.getID(), user.getName()));
            logger.error(e.getMessage());
            return 2;
        }
    }

    public int downloadCape(MCUser user, InputStream inputStream) {
        try {
            BufferedImage img = ImageIO.read(inputStream);
            int width = img.getWidth();
            int height = img.getHeight();
            if (!((width % 64 == 0 && height % 32 == 0) && (width <= 512 && height <= 512))) {
                return 1;
            }
            Path path = this.capeDir.resolve(user.getUUID());
            File cape = new File(path.toUri());
            if (cape.createNewFile()) {
                logger.debug(String.format("Created cape file for [id:name]: %s:%s", user.getID(), user.getName()));
            }
            ImageIO.write(img, "png", cape);
            logger.info(String.format("Downloaded cape for [id:name]: %s:%s", user.getID(), user.getName()));
            return 0;
        } catch (IOException e) {
            logger.error(String.format("Failed to download cape for [id:name]: %s:%s", user.getID(), user.getName()));
            logger.error(e.getMessage());
            return 2;
        }
    }

    public int removeCape(MCUser user) {
        Path capePath = this.capeDir.resolve(user.getUUID());
        if (capePath.toFile().exists()) {
            if (capePath.toFile().delete()) {
                logger.info(String.format("Removed cape for [id:name]: %s:%s", user.getID(), user.getName()));
                return 0;
            } else {
                logger.error(String.format("Failed to remove cape for [id:name]: %s:%s", user.getID(), user.getName()));
                return 2;
            }
        } else {
            return 1;
        }
    }

    public Skin getSkin(MCUser user) {
        try {
            Path skinPath = this.skinDir.resolve(user.getUUID());
            if (!skinPath.toFile().exists()) {
                skinPath = this.defaultSkin;
            }
            InputStream inputStream = Files.newInputStream(skinPath);
            BufferedImage img = ImageIO.read(inputStream);
            int width = img.getWidth();
            int fraction = width / 8;
            int x = (int) (fraction * 6.75);
            int y = (int) (fraction * 2.5);
            int pixel = img.getRGB(x, y);
            inputStream.close();
            logger.info(String.format("Resolved skin for [id:name]: %s:%s", user.getID(), user.getName()));
            return new Skin(Files.newInputStream(skinPath), pixel == 0);
        } catch (IOException e) {
            logger.error(String.format("Failed to get skin for [id:name]: %s:%s", user.getID(), user.getName()));
            logger.error(e.getMessage());
            return null;
        }
    }

    public Cape        getCape(MCUser user) {
        try {
            Path capePath = this.capeDir.resolve(user.getUUID());
            if (!capePath.toFile().exists()) {
                capePath = this.defaultCape;
            }
            logger.info(String.format("Resolved cape for [id:name]: %s:%s", user.getID(), user.getName()));
            return new Cape(Files.newInputStream(capePath));
        } catch (IOException e) {
            logger.error(String.format("Failed to get cape for [id:name]: %s:%s", user.getID(), user.getName()));
            logger.error(e.getMessage());
            return null;
        }
    }
}
