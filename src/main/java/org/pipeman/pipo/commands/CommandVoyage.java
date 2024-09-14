package org.pipeman.pipo.commands;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.utils.FileUpload;
import org.pipeman.pipo.Pipo;
import org.pipeman.pipo.Utils;
import org.pipeman.pipo.offline.Offlines;
import org.pipeman.pipo.storage.PlayerDiscordRegistry;

import java.awt.*;
import java.util.Optional;
import java.util.UUID;

public class CommandVoyage {

    public static String VOYAGES_ROLE_ID = "1280140385061965824";

    public static void handle(SlashCommandInteractionEvent event) {

        Optional<UUID> uuid = Pipo.getInstance().discordRegistry.getPlayerUuid(event.getMember().getId());

        if (uuid.isEmpty()) {
            event.reply("You need to link your Minecraft account to your Discord account first")
                    .setEphemeral(true)
                    .queue();
            return;
        }

        String playerName = Offlines.getNameByUUID(uuid.get()).orElse("Unknown");

        String title = event.getOption("title").getAsString();
        String secondaryTitle = event.getOption("secondary-title").getAsString();
        String description = event.getOption("description").getAsString();
        String logoUrl = event.getOption("logo-url").getAsString();
        String imageUrl = event.getOption("image-url").getAsString();

        Role updateRole = event.getGuild().getRoleById(VOYAGES_ROLE_ID);
        event.getChannel().sendMessage("|| " + updateRole.getAsMention() + " ||").queue();

        event.deferReply().queue();
        event.getHook().sendFiles(FileUpload.fromData(Utils.getHeadSkin(playerName), playerName.hashCode() + ".png"))
                .setEmbeds(createEmbed(playerName, title, secondaryTitle, description, logoUrl, imageUrl))
                .queue();
    }

    public static MessageEmbed createEmbed(
            String playerName, String title, String secondaryTitle, String description, String logoUrl, String imageUrl) {
        EmbedBuilder builder = new EmbedBuilder()
                .setColor(new Color(59, 152, 0));

        builder.setTitle(title);

        builder.addField(
                secondaryTitle,
                description.replace("\\n", "\n"),
                false
        );

        builder.setFooter("Vogayes");

        String filename = playerName.hashCode() + ".png";
        builder.setAuthor(playerName, null, "attachment://" + filename);

        builder.setThumbnail(logoUrl);
        builder.setImage(imageUrl);

        return builder.build();
    }
}