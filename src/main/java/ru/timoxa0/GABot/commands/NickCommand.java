package ru.timoxa0.GABot.commands;

import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import ru.timoxa0.GABot.handlers.TranslationHandler;
import ru.timoxa0.GABot.models.MCUser;

import java.util.Collections;
import java.util.Objects;

public class NickCommand extends SlashCommand {
    private final TranslationHandler trn = TranslationHandler.getTranslationHandler();

    public NickCommand() {
        this.name = "nick";
        this.help = trn.getProperty("commands.nick.description");
        this.options = Collections.singletonList(new OptionData(
                OptionType.STRING, trn.getProperty("options.nick"),
                trn.getProperty("options.nick.description")).setRequired(true)
        );
    }

    @Override
    protected void execute(SlashCommandEvent event) {
        MCUser user = MCUser.getByID(Objects.requireNonNull(Objects.requireNonNull(event.getMember()).getId()));
        if (user != null) {
            if (user.setName(event.getOptions().get(0).getAsString()).commit()) {
                event.reply(trn.getProperty("commands.nick.success")).setEphemeral(true).queue();
            } else {
                event.reply(trn.getProperty("misc.command.error")).setEphemeral(true).queue();
            }
        } else {
            event.reply(trn.getProperty("commands.nick.failed")).setEphemeral(true).queue();
        }
    }
}
