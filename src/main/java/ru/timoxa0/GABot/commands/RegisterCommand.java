package ru.timoxa0.GABot.commands;

import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.apache.logging.log4j.LogManager;
import ru.timoxa0.GABot.handlers.TranslationHandler;
import ru.timoxa0.GABot.models.MCUser;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class RegisterCommand extends SlashCommand {
    private final TranslationHandler trn = TranslationHandler.getTranslationHandler();

    public RegisterCommand() {
        List<OptionData> options = new ArrayList<>();
        options.add(new OptionData(
                OptionType.STRING, trn.getProperty("options.name"),
                trn.getProperty("options.name.description")).setRequired(true)
        );
        options.add(new OptionData(
                OptionType.STRING, trn.getProperty("options.password"),
                trn.getProperty("options.password.description")).setRequired(true)
        );

        this.name = "register";
        this.help = trn.getProperty("commands.register.description");
        this.options = options;
        this.aliases = new String[] {"reg"};
        LogManager.getLogger(this.getClass()).info(String.format("Added command: /%s", this.name));
    }

    @Override
    protected void execute(SlashCommandEvent event) {
        if (!MCUser.checkFor(Objects.requireNonNull(event.getMember()).getId())) {
            MCUser user = new MCUser(Objects.requireNonNull(event.getMember()).getId(),
                    event.getOptions().get(0).getAsString(),
                    event.getOptions().get(1).getAsString()
            );
            if (user.commit()) {
                event.reply(trn.getProperty("commands.register.success")).setEphemeral(true).queue();
            } else {
                event.reply(trn.getProperty("misc.command.error")).setEphemeral(true).queue();
            }
        } else {
            event.reply(trn.getProperty("commands.register.failed")).setEphemeral(true).queue();
        }
    }
}
