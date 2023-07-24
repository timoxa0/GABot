package ru.timoxa0.GABot.commands;

import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import ru.timoxa0.GABot.handlers.ConfigHandler;
import ru.timoxa0.GABot.handlers.TranslationHandler;

import java.awt.*;

public class HelpCommand extends SlashCommand {
    private final TranslationHandler trn = TranslationHandler.getTranslationHandler();
    private final ConfigHandler cfg = ConfigHandler.getConfigHandler();
    public HelpCommand() {
        this.name = "help";
        this.help = trn.getProperty("commands.help.description");
    }

    @Override
    protected void execute(SlashCommandEvent event) {
        MessageEmbed embed = new EmbedBuilder()
                .setTitle(trn.getProperty("help.title"))
                .setDescription("help.description")
                .setColor(Color.decode(cfg.getProperty("ds.bot.embed.color")))
                .addField("/register",      trn.getProperty("help.commands.register"),  true)
                .addBlankField(true)
                .addField("/password",      trn.getProperty("help.commands.password"),  true)
                .addField("/skin",          trn.getProperty("help.commands.skin"),      true)
                .addBlankField(true)
                .addField("/skin",          trn.getProperty("help.commands.skin"),      true)
                .addField("/cape",          trn.getProperty("help.commands.cape"),      true)
                .addBlankField(true)
                .addField("/cape",          trn.getProperty("help.commands.cape"),      true)
                .addField("/links",         trn.getProperty("help.commands.links"),     true)
                .addBlankField(true)
                .addField("/help",          trn.getProperty("help.commands.help"),      true)
                .build();
        event.reply("")
                .setEphemeral(true)
                .setEmbeds(embed)
                .queue();
    }
}
