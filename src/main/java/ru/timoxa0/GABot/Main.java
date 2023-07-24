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
}