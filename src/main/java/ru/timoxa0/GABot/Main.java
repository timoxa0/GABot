package ru.timoxa0.GABot;

import com.jagrosh.jdautilities.command.CommandClientBuilder;
import io.undertow.Undertow;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.timoxa0.GABot.commands.*;
import ru.timoxa0.GABot.handlers.*;
import sun.misc.Signal;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Scanner;

public class Main {
    private static final Logger logger = LogManager.getLogger(Main.class);

    public static void main(String[] args) {

        try {
            if (ConfigHandler.createConfigHandler(Path.of("bot.config").toFile())) {
                try {
                    run(ConfigHandler.getConfigHandler());
                } catch (IllegalArgumentException e) {
                    logger.fatal("Config error: " + e.getMessage());
                }
            } else {
                logger.info("Config file incorrect or not found. Creating new one");
                createConfig(ConfigHandler.getConfigHandler());
                System.exit(0);
            }
        } catch (IOException e) {
            logger.fatal("Failed to load config file!");
            logger.fatal(e.getMessage());
            System.exit(1);
        }
    }

    private static void createConfig(ConfigHandler cfg) {
        String tmp;

        cfg.setProperty("ds.bot.token", input("discord bot token"));
        cfg.setProperty("ds.bot.guild.id", input("discord guild id"));
        cfg.setProperty("ds.bot.owner.id", input("bot owner id", "0"));
        cfg.setProperty("ds.bot.translation", input("bot language (en-us | ru)", "ru"));
        cfg.setProperty("ds.bot.status", input("bot status (online|idle|dnd|invisible|offline)", "online"));
        cfg.setProperty("ds.bot.activity.type", input("bot activity type (playing|steaming|watching|listening|competing)", "playing"));
        cfg.setProperty("ds.bot.activity.text", input("bot activity text"));
        cfg.setProperty("ds.bot.links.exe", input("link to launcher.exe"));
        cfg.setProperty("ds.bot.links.jar", input("link to launcher.jar"));
        cfg.setProperty("ds.bot.embed.color", input("bot links and help embeds color", "0x000000"));

        tmp = input("bot embeds author name", "None");
        if (!tmp.equals("None")) { cfg.setProperty("ds.bot.embed.author.name", tmp); }
        tmp = input("bot embeds author url", "None");
        if (!tmp.equals("None")) { cfg.setProperty("ds.bot.embed.author.url", tmp); }
        tmp = input("bot embeds author icon url", "None");
        if (!tmp.equals("None")) { cfg.setProperty("ds.bot.embed.author.icon", tmp); }

        cfg.setProperty("db.connectionURL", String.format("jdbc:mariadb://%s:%s/%s",
                input("database host"),
                input("database port", "3306"),
                input("database name")
        ));
        cfg.setProperty("db.table", input("database table"));
        cfg.setProperty("db.username", input("database username"));
        cfg.setProperty("db.password", input("database password"));
        cfg.setProperty("db.columns.id", input("database id column name", "id"));
        cfg.setProperty("db.columns.uuid", input("database uuid column name", "uuid"));
        cfg.setProperty("db.columns.username", input("database username column name", "username"));
        cfg.setProperty("db.columns.password", input("database password column name", "password"));

        cfg.setProperty("tp.server.host", input("texture provider host", "127.0.0.1"));
        cfg.setProperty("tp.server.port", input("texture provider port", "9275"));

        cfg.flush();
    }

    private static void run(ConfigHandler cfg) throws IllegalArgumentException {
        try {
            TranslationHandler.createTranslationHandler(cfg.getProperty("ds.bot.translation"));
            logger.info("Loaded bot language: " + TranslationHandler.getTranslationHandler().getProperty("translation.lang"));
        } catch (IOException e) {
            logger.fatal("Failed to load translation file!");
            logger.fatal(e.getMessage());
            System.exit(1);
        }

        try {
            TextureProvider.createTextureHandler(
                    Path.of(cfg.getProperty("tp.dirs.skins")),
                    Path.of(cfg.getProperty("tp.dirs.capes")),
                    Path.of(cfg.getProperty("tp.defaults.skin")),
                    Path.of(cfg.getProperty("tp.defaults.cape"))
            );
        } catch (Throwable e) {
            logger.fatal("Failed to create texture provider");
            logger.fatal(e.getMessage());
            System.exit(1);
        }

        DBHandler.createDBHandler(
                cfg.getProperty("db.connectionURL"),
                cfg.getProperty("db.username"), cfg.getProperty("db.password"), cfg.getProperty("db.table"),
                cfg.getProperty("db.columns.id"), cfg.getProperty("db.columns.uuid"),
                cfg.getProperty("db.columns.username"), cfg.getProperty("db.columns.password")
        );

        CommandClientBuilder builder = new CommandClientBuilder()
                .setOwnerId(cfg.getProperty("ds.bot.owner.id", "0"))
                .useHelpBuilder(false)
                .addSlashCommands(
                        new HelpCommand(),
                        new RegisterCommand(),
                        new NameCommand(),
                        new PasswordCommand(),
                        new SkinCommand(),
                        new CapeCommand(),
                        new LinksCommand()
                )
                .setStatus(OnlineStatus.fromKey(cfg.getProperty("ds.bot.status").toLowerCase()))
                .setActivity(Activity.of(
                        Activity.ActivityType.valueOf(cfg.getProperty("ds.bot.activity.type").toUpperCase()),
                        cfg.getProperty("ds.bot.activity.text"))
                );

        if (!cfg.getProperty("ds.bot.guild.id", "").isEmpty()) {
            builder.forceGuildOnly(cfg.getProperty("ds.bot.guild.id"));
            logger.info("Forcing bot to be guild only. Guild id: " + cfg.getProperty("ds.bot.guild.id"));
        } else {
            logger.warn("Guild ID not set. Bot will update commands on all servers. Command updates will be slower");
        }

        JDA jda = JDABuilder.createDefault(cfg.getProperty("ds.bot.token"))
                .addEventListeners(builder.build())
                .enableIntents(GatewayIntent.DIRECT_MESSAGES, GatewayIntent.GUILD_MEMBERS)
                .build();

        Undertow server = Undertow.builder()
                .addHttpListener(Integer.parseInt(cfg.getProperty("tp.server.port")), cfg.getProperty("tp.server.host"))
                .setHandler(new APIHandler())
                .build();
        server.start();

        Signal.handle(new Signal("INT"), signal -> {
                    logger.info("Caught SIGINT -> Performing shutdown");
                    jda.shutdownNow();
                    server.stop();
                    System.exit(0);
                }
        );
    }

    private static String input(String query) {
        String response;
        do {
            System.out.printf("Enter %s: ", query);
            Scanner sc = new Scanner(System.in);
            response = sc.nextLine();
        } while (response.isEmpty());
        return response.trim();
    }

    private static String input(String query, String fallback) {
        String response;
        System.out.printf("Enter %s [%s]: ", query, fallback);
        Scanner sc = new Scanner(System.in);
        response = sc.nextLine();
        if (response.isEmpty()) {
            return fallback;
        } else {
            return response.trim();
        }
    }
}