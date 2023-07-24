package ru.timoxa0.GABot.commands;

import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.timoxa0.GABot.handlers.TextureProvider;
import ru.timoxa0.GABot.handlers.TranslationHandler;
import ru.timoxa0.GABot.models.MCUser;

import java.util.Collections;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

public class CapeCommand extends SlashCommand {
    private static final TranslationHandler trn = TranslationHandler.getTranslationHandler();
    private static final TextureProvider TEXTURE_PROVIDER = TextureProvider.getTextureHandler();
    private final static Logger logger = LogManager.getLogger(CapeCommand.class);

    public CapeCommand() {
        this.name = "cape";
        this.help = trn.getProperty("commands.cape.description");
        this.children = new SlashCommand[]{new Upload(), new Remove()};
    }

    @Override
    protected void execute(SlashCommandEvent event) {
    }

    private static class Upload extends SlashCommand {
        public Upload() {
            this.name = trn.getProperty("commands.cape.upload.name");
            this.help = trn.getProperty("commands.cape.upload.description");
            this.options = Collections.singletonList(new OptionData(OptionType.ATTACHMENT,
                    trn.getProperty("options.cape.upload.filename"), trn.getProperty("options.cape.upload.description"))
                    .setRequired(true)
            );
        }

        @Override
        protected void execute(SlashCommandEvent event) {
            MCUser user = MCUser.getByID(Objects.requireNonNull(Objects.requireNonNull(event.getMember()).getId()));
            if (user != null) {
                Message.Attachment attachment = event.getOptions().get(0).getAsAttachment();
                try {
                    switch (TEXTURE_PROVIDER.downloadCape(user, attachment.getProxy().download().get())) {
                        case 0 -> event.reply(trn.getProperty("commands.cape.upload.success")).setEphemeral(true)
                                .queue();
                        case 1 -> event.reply(trn.getProperty("commands.cape.upload.incorrect")).setEphemeral(true)
                                .queue();
                        case 2 -> event.reply(trn.getProperty("misc.command.error")).setEphemeral(true)
                                .queue();
                    }
                } catch (InterruptedException | ExecutionException e) {
                    logger.error(e.getMessage());
                }
            } else {
                event.reply(trn.getProperty("commands.cape.upload.failed")).setEphemeral(true).queue();
            }
        }
    }

    private static class Remove extends SlashCommand {
        public Remove() {
            this.name = trn.getProperty("commands.cape.remove.name");
            this.help = trn.getProperty("commands.cape.remove.description");
        }

        @Override
        protected void execute(SlashCommandEvent event) {
            switch (TEXTURE_PROVIDER.removeCape(MCUser.getByID(Objects.requireNonNull(event.getMember()).getId()))) {
                case 0 -> event.reply(trn.getProperty("commands.cape.remove.success")).setEphemeral(true).queue();
                case 1 -> event.reply(trn.getProperty("commands.cape.remove.failed")).setEphemeral(true).queue();
                case 2 -> event.reply(trn.getProperty("misc.command.error")).setEphemeral(true).queue();
            }
        }
    }
}
