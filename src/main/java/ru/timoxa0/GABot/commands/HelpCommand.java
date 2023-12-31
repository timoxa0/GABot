package ru.timoxa0.GABot.commands;

import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import org.apache.logging.log4j.LogManager;
import ru.timoxa0.GABot.handlers.ConfigHandler;
import ru.timoxa0.GABot.handlers.TranslationHandler;

import java.awt.*;

public class HelpCommand extends SlashCommand {
    private final TranslationHandler trn = TranslationHandler.getTranslationHandler();
    private final ConfigHandler cfg = ConfigHandler.getConfigHandler();

    public HelpCommand() {
        this.name = "help";
        this.help = trn.getProperty("commands.help.description");
        LogManager.getLogger(this.getClass()).info(String.format("Added command: /%s", this.name));
    }

    @Override
    protected void execute(SlashCommandEvent event) {
        EmbedBuilder embed = new EmbedBuilder()
                .setTitle(trn.getProperty("help.title"))
                .setDescription(trn.getProperty("help.description"))
                .setColor(Color.decode(cfg.getProperty("ds.bot.embed.color")))
                .addField("/register", trn.getProperty("help.commands.register"), true)
                .addBlankField(true)
                .addField("/password", trn.getProperty("help.commands.password"), true)
                .addField("/skin " + trn.getProperty("commands.skin.upload.name"), trn.getProperty("help.commands.skin.upload"), true)
                .addBlankField(true)
                .addField("/skin " + trn.getProperty("commands.skin.mojang.name"), trn.getProperty("help.commands.skin.mojang"), true)
                .addField("/cape " + trn.getProperty("commands.cape.upload.name"), trn.getProperty("help.commands.cape.upload"), true)
                .addBlankField(true)
                .addField("/cape " + trn.getProperty("commands.cape.remove.name"), trn.getProperty("help.commands.cape.remove"), true)
                .addField("/links", trn.getProperty("help.commands.links"), true)
                .addBlankField(true)
                .addField("/help", trn.getProperty("help.commands.help"), true);

        if (!cfg.getProperty("ds.bot.embed.author.name", "").isEmpty()) {
            if (!cfg.getProperty("ds.bot.embed.author.url", "").isEmpty()) {
                if (!cfg.getProperty("ds.bot.embed.author.icon", "").isEmpty()) {
                    embed.setAuthor(
                            cfg.getProperty("ds.bot.embed.author.name"),
                            cfg.getProperty("ds.bot.embed.author.url"),
                            cfg.getProperty("ds.bot.embed.author.icon")
                    );
                } else {
                    embed.setAuthor(
                            cfg.getProperty("ds.bot.embed.author.name"),
                            cfg.getProperty("ds.bot.embed.author.url")
                    );
                }
            } else {
                embed.setAuthor(cfg.getProperty("ds.bot.embed.author.name"));
            }
        }

        event.reply("")
                .setEphemeral(true)
                .setEmbeds(embed.build())
                .queue();
    }
}
