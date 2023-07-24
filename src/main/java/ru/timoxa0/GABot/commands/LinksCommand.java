package ru.timoxa0.GABot.commands;

import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.apache.logging.log4j.LogManager;
import ru.timoxa0.GABot.handlers.ConfigHandler;
import ru.timoxa0.GABot.handlers.TranslationHandler;

import java.awt.*;

public class LinksCommand extends SlashCommand {
    private final TranslationHandler trn = TranslationHandler.getTranslationHandler();
    private final ConfigHandler cfg = ConfigHandler.getConfigHandler();

    public LinksCommand() {
        this.name = "links";
        this.help = trn.getProperty("commands.links.description");
        LogManager.getLogger(this.getClass()).info(String.format("Added command: /%s", this.name));
    }

    @Override
    protected void execute(SlashCommandEvent event) {
        String exeLink;
        String jarLink;
        if (trn.getProperty("links.exe.name", "").isEmpty()) {
            exeLink = cfg.getProperty("ds.bot.links.exe");
        } else {
            exeLink = String.format("[%s](%s)", trn.getProperty("links.exe.name"), cfg.getProperty("ds.bot.links.exe"));
        }

        if (trn.getProperty("links.jar.name", "").isEmpty()) {
            jarLink = cfg.getProperty("ds.bot.links.jar");
        } else {
            jarLink = String.format("[%s](%s)", trn.getProperty("links.jar.name"), cfg.getProperty("ds.bot.links.jar"));
        }

        EmbedBuilder embed = new EmbedBuilder()
                .setTitle(trn.getProperty("links.title"))
                .setDescription(trn.getProperty("links.description"))
                .setColor(Color.decode(cfg.getProperty("ds.bot.embed.color")))
                .addField(trn.getProperty("links.exe.os"), exeLink, true)
                .addField(trn.getProperty("links.jar.os"), jarLink, true);

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
