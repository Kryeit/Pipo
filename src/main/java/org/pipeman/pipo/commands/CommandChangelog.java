package org.pipeman.pipo.commands;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

import java.awt.*;
import java.util.Arrays;

public class CommandChangelog {

    public static String UPDATE_ROLE_ID = "1118550893021892650";

    public static void handle(SlashCommandInteractionEvent event) {
        String changelog = event.getOption("changelog").getAsString();
        String version = event.getOption("version").getAsString();
        boolean update = Boolean.TRUE.equals(event.getOption("update").getAsBoolean());

        Role updateRole = event.getGuild().getRoleById(UPDATE_ROLE_ID);
        if (update)
            event.getChannel().sendMessage(updateRole.getAsMention()).queue();

        event.replyEmbeds(createEmbed(changelog, version, update))
                .mentionRepliedUser(false)
                .queue();
    }

    public static MessageEmbed createEmbed(String changelog, String version, boolean update) {


        EmbedBuilder builder = new EmbedBuilder()
                .setColor(new Color(59, 152, 0));

        // Separate the changelog into lines
        String[] changelogs = changelog.split("\n");

        if (update) {
            builder.setTitle("Kryeit " + version, "https://modrinth.com/modpack/kryeit/version/" + version);
        } else {
            builder.setTitle("Kryeit " + version);
        }

        builder.addField(
                "Changelog",
                changelog.replace("\\n", "\n"),
                false
        );

        builder.setFooter("Kryeit Team");
        return builder.build();
    }
}
