package org.pipeman.pipo.commands;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.awt.*;

public class CommandVote {
    public static void handle(SlashCommandInteractionEvent event) {
        event.replyEmbeds(createEmbed())
                .mentionRepliedUser(false)
                .queue();
    }

    public static MessageEmbed createEmbed() {
        EmbedBuilder builder = new EmbedBuilder()
                .setColor(new Color(59, 152, 0))
                .setTitle("Voting site", "https://www.planetminecraft.com/server/kryeit-5584167/vote/");

        builder.addField(
                "Link",
                "https://www.planetminecraft.com/server/kryeit-5584167/vote/",
                false
        );

        builder.setThumbnail("https://raw.githubusercontent.com/CMD-Golem/CMD-Golem-Website/master/elements/nav/Planet%20Minecraft.svg");
        return builder.build();
    }
}
