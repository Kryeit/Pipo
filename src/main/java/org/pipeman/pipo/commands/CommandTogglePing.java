package org.pipeman.pipo.commands;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.pipeman.pipo.Pipo;

import java.awt.*;
import java.util.Optional;
import java.util.UUID;

public class CommandTogglePing {
    public static void handle(SlashCommandInteractionEvent event) {
        String id = event.getUser().getId();
        Optional<UUID> uuid = Pipo.getInstance().discordRegistry.getPlayerUuid(id);

        if (uuid.isEmpty()) return;

        Optional<Boolean> ping = Pipo.getInstance().playerTogglePing.isPingEnabled(uuid.get());
        if (ping.isEmpty()) return;

        Pipo.getInstance().playerTogglePing.togglePing(uuid.get(), !ping.get());

        event.replyEmbeds(createEmbed(!ping.get()))
                .mentionRepliedUser(false)
                .queue();
    }

    public static MessageEmbed createEmbed(boolean ping) {
        EmbedBuilder builder = new EmbedBuilder()
                .setColor(new Color(59, 152, 0))
                .setTitle("Ping toggled");

        builder.addField(
                "Your ping is now " +
                        (ping ? "enabled" : "disabled") +
                        ". You will now " +
                        (ping ? "receive" : "not receive") +
                        " pings when the server starts.",
                "Use the command again to toggle it back.",
                false
        );
        return builder.build();
    }
}
