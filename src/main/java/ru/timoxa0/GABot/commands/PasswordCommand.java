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

public class PasswordCommand extends SlashCommand {
    private final TranslationHandler trn = TranslationHandler.getTranslationHandler();

    public PasswordCommand() {
        this.name = "password";
        this.help = trn.getProperty("commands.password.description");
        this.options = Collections.singletonList(new OptionData(
                OptionType.STRING, trn.getProperty("options.password"),
                trn.getProperty("options.password.description")).setRequired(true)
        );
        LogManager.getLogger(this.getClass()).info(String.format("Added command: /%s", this.name));
    }

    @Override
    protected void execute(SlashCommandEvent event) {
        MCUser user = MCUser.getByID(Objects.requireNonNull(Objects.requireNonNull(event.getMember()).getId()));
        if (user != null) {
            if (user.setPassword(event.getOptions().get(0).getAsString()).commit()) {
                event.reply(trn.getProperty("commands.password.success")).setEphemeral(true).queue();
            } else {
                event.reply(trn.getProperty("misc.command.error")).setEphemeral(true).queue();
            }
        } else {
            event.reply(trn.getProperty("commands.password.failed")).setEphemeral(true).queue();
        }
    }
}
