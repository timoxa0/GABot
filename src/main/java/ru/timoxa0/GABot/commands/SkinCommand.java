package ru.timoxa0.GABot.commands;

import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.apache.commons.codec.binary.Base64;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import ru.timoxa0.GABot.handlers.TextureProvider;
import ru.timoxa0.GABot.handlers.TranslationHandler;
import ru.timoxa0.GABot.models.MCUser;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

public class SkinCommand extends SlashCommand {
    private static final TranslationHandler trn = TranslationHandler.getTranslationHandler();
    private static final TextureProvider TEXTURE_PROVIDER = TextureProvider.getTextureHandler();
    private final static Logger logger = LogManager.getLogger(SkinCommand.class);

    public SkinCommand() {
        this.name = "skin";
        this.help = trn.getProperty("commands.skin.description");
        this.children = new SlashCommand[]{new Upload(), new Mojang()};
        logger.info(String.format("Added command: /%s", this.name));
    }

    @Override
    protected void execute(SlashCommandEvent event) {
    }

    private static class Upload extends SlashCommand {
        public Upload() {
            this.name = trn.getProperty("commands.skin.upload.name");
            this.help = trn.getProperty("commands.skin.upload.description");
            this.options = Collections.singletonList(new OptionData(OptionType.ATTACHMENT,
                    trn.getProperty("options.skin.upload.filename"), trn.getProperty("options.skin.upload.description"))
                    .setRequired(true));
        }

        @Override
        protected void execute(SlashCommandEvent event) {
            MCUser user = MCUser.getByID(Objects.requireNonNull(Objects.requireNonNull(event.getMember()).getId()));
            if (user != null) {
                Message.Attachment attachment = event.getOptions().get(0).getAsAttachment();
                try {
                    switch (TEXTURE_PROVIDER.downloadSkin(user, attachment.getProxy().download().get())) {
                        case 0 -> event.reply(trn.getProperty("commands.skin.upload.success")).setEphemeral(true)
                                .queue();
                        case 1 -> event.reply(trn.getProperty("commands.skin.incorrect")).setEphemeral(true)
                                .queue();
                        case 2 -> event.reply(trn.getProperty("misc.command.error")).setEphemeral(true)
                                .queue();
                    }
                } catch (InterruptedException | ExecutionException e) {
                    event.reply(trn.getProperty("misc.command.error")).setEphemeral(true).queue();
                    logger.error(e.getMessage());
                }
            } else {
                event.reply(trn.getProperty("commands.skin.upload.failed")).setEphemeral(true).queue();
            }
        }
    }

    private static class Mojang extends SlashCommand {

        public Mojang() {
            this.name = trn.getProperty("commands.skin.mojang.name");
            this.help = trn.getProperty("commands.skin.mojang.description");
            this.options = Collections.singletonList(new OptionData(OptionType.STRING,
                    trn.getProperty("options.skin.mojang.username"), trn.getProperty("options.skin.mojang.description"))
                    .setRequired(true));
        }

        private static String readAll(Reader rd) throws IOException {
            StringBuilder sb = new StringBuilder();
            int cp;
            while ((cp = rd.read()) != -1) {
                sb.append((char) cp);
            }
            return sb.toString();
        }

        public static JSONObject readJsonFromUrl(String url) throws IOException, JSONException {
            try (InputStream is = new URL(url).openStream()) {
                BufferedReader rd = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
                String jsonText = readAll(rd);
                return new JSONObject(jsonText);
            }
        }

        @Override
        protected void execute(SlashCommandEvent event) {
            MCUser user = MCUser.getByID(Objects.requireNonNull(event.getMember()).getId());
            if (user != null) {
                try {
                    String userid_url = "https://api.mojang.com/users/profiles/minecraft/";
                    String userinfo_url = "https://sessionserver.mojang.com/session/minecraft/profile/";
                    String userid = readJsonFromUrl(userid_url + event.getOptions().get(0).getAsString())
                            .get("id").toString();
                    String encodedTextureData = ((JSONObject) ((JSONArray) readJsonFromUrl(userinfo_url + userid)
                            .get("properties")).get(0)).get("value").toString();
                    String skinUrl = ((JSONObject) ((JSONObject) new JSONObject(
                            new String(Base64.decodeBase64(encodedTextureData), StandardCharsets.UTF_8))
                            .get("textures")).get("SKIN")).get("url").toString();

                    switch (TEXTURE_PROVIDER.downloadSkin(user, new URL(skinUrl).openStream())) {
                        case 0 -> event.reply(trn.getProperty("commands.skin.mojang.success")).setEphemeral(true)
                                .queue();
                        case 1 -> event.reply(trn.getProperty("commands.skin.incorrect")).setEphemeral(true)
                                .queue();
                        case 2 -> event.reply(trn.getProperty("misc.command.error")).setEphemeral(true)
                                .queue();
                    }
                } catch (FileNotFoundException ignored) {
                    event.reply(trn.getProperty("commands.skin.mojang.api.notfound")).setEphemeral(true).queue();
                } catch (IOException e) {
                    event.reply(trn.getProperty("misc.command.error")).setEphemeral(true).queue();
                    logger.error(e.getMessage());
                }
            } else {
                event.reply(trn.getProperty("commands.skin.mojang.failed")).setEphemeral(true).queue();
            }
        }

    }
}
