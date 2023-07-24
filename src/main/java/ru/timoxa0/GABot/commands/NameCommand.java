package ru.timoxa0.GABot.commands;

import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.apache.logging.log4j.LogManager;
import ru.timoxa0.GABot.handlers.TranslationHandler;
import ru.timoxa0.GABot.models.MCUser;

import java.util.Collections;
import java.util.Objects;

public class NameCommand extends SlashCommand {
    private final TranslationHandler trn = TranslationHandler.getTranslationHandler();

    public NameCommand() {
        this.name = "name";
        this.help = trn.getProperty("commands.name.description");
        this.options = Collections.singletonList(new OptionData(
                OptionType.STRING, trn.getProperty("options.name"),
                trn.getProperty("options.name.description")).setRequired(true)
        );
        LogManager.getLogger(this.getClass()).info(String.format("Added command: /%s", this.name));
    }

    @Override
    protected void execute(SlashCommandEvent event) {
        MCUser user = MCUser.getByID(Objects.requireNonNull(Objects.requireNonNull(event.getMember()).getId()));
        if (user != null) {
            if (user.setName(event.getOptions().get(0).getAsString()).commit()) {
                event.reply(trn.getProperty("commands.name.success")).setEphemeral(true).queue();
            } else {
                event.reply(trn.getProperty("misc.command.error")).setEphemeral(true).queue();
            }
        } else {
            event.reply(trn.getProperty("commands.name.failed")).setEphemeral(true).queue();
        }
    }
}
