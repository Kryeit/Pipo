package org.pipeman.pipo.commands;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.pipeman.pipo.Utils;
import org.pipeman.pipo.tps.Lag;

import java.awt.*;
import java.text.DecimalFormat;

public class CommandTPS {
    private static final DecimalFormat format = new DecimalFormat("#.##");

    public static void handle(SlashCommandInteractionEvent event) {

        MessageEmbed embed = new EmbedBuilder()
                .setTitle("Kryeit.com", "https://kryeit.com")
                .addField("There are " + Utils.getOnlinePlayersSize() + " players online", "TPS: " + format.format(Lag.getTPS()), false)
                .setColor(new Color(59, 152, 0))
                .build();

        event.replyEmbeds(embed).queue();
    }
}
