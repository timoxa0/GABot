package ru.timoxa0.GABot;

import com.jagrosh.jdautilities.command.CommandClient;
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

public class Main {
    private static final Logger logger = LogManager.getLogger(Main.class);

    public static void main(String[] args) {
        ConfigHandler cfg = null;

        try {
            cfg = ConfigHandler.getConfigHandler(Path.of("bot.properties"));
        } catch (IOException e) {
            logger.fatal("Failed to load config file!");
            logger.fatal(e.getMessage());
            System.exit(1);
        }

        try {
            TranslationHandler.createTranslationHandler(Path.of(cfg.getProperty("ds.bot.translation")));
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

        CommandClientBuilder builder = new CommandClientBuilder();
        CommandClient commandClient = builder.setOwnerId(cfg.getProperty("ds.bot.owner.id"))
                .forceGuildOnly(cfg.getProperty("ds.bot.guild.id"))
                .addSlashCommands(
                        new HelpCommand(),
                        new RegisterCommand(),
                        new NickCommand(),
                        new PasswordCommand(),
                        new SkinCommand(),
                        new CapeCommand(),
                        new LinksCommand()
                )
                .setStatus(OnlineStatus.IDLE)
                .setActivity(Activity.of(
                        Activity.ActivityType.valueOf(cfg.getProperty("ds.bot.status.activity").toUpperCase()),
                        cfg.getProperty("ds.bot.status.text")))
                .useHelpBuilder(false)
                .build();
        JDA jda = JDABuilder.createDefault(cfg.getProperty("ds.bot.token"))
                .addEventListeners(commandClient)
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
}