package org.pipeman.pipo.commands;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import org.pipeman.pipo.Leaderboard;
import org.pipeman.pipo.Leaderboard.LeaderboardEntry;
import org.pipeman.pipo.Leaderboard.Order;
import org.pipeman.pipo.Utils;

import java.awt.*;
import java.text.MessageFormat;
import java.util.List;

public class CommandTopN {
    public static void handle(SlashCommandInteractionEvent event) {
        int limit = event.getOption("limit", 10, OptionMapping::getAsInt);
        int offset = event.getOption("offset", 0, OptionMapping::getAsInt);
        boolean sortDescending = event.getOption("sort-direction", true, OptionMapping::getAsBoolean);
        String orderBy = event.getOption("order-by", "playtime", OptionMapping::getAsString);

        if (limit < 1) {
            event.replyEmbeds(Utils.createErrorEmbed("Limit must be greater than 0"))
                    .setEphemeral(true)
                    .queue();
            return;
        }

        if (offset < 0) {
            event.replyEmbeds(Utils.createErrorEmbed("Offset must be greater than or equal to 0"))
                    .setEphemeral(true)
                    .queue();
            return;
        }

        Order order = sortDescending ? Order.DESC : Order.ASC;
        List<LeaderboardEntry> leaderboard = Leaderboard.getLeaderboard(order, orderBy, limit, offset);
        if (leaderboard.isEmpty()) {
            event.replyEmbeds(Utils.createErrorEmbed("Offset must be less than the leaderboard's length"))
                    .setEphemeral(true)
                    .queue();
            return;
        }

        replyWithLeaderboard(leaderboard, offset + 1, event);
    }

    public static void handleTop10(SlashCommandInteractionEvent event) {
        replyWithLeaderboard(Leaderboard.getLeaderboard(Order.DESC, "playtime", 10), 1, event);
    }

    private static void replyWithLeaderboard(List<LeaderboardEntry> entries, int startRank, SlashCommandInteractionEvent event) {
        event.deferReply().queue();

        StringBuilder content = new StringBuilder();
        int rank = startRank;
        for (LeaderboardEntry le : entries) {
            String newLine = MessageFormat.format(
                    "**{0}:** {1} ({2})\n",
                    rank, Utils.escapeName(le.name()), le.value().formatted()
            );
            if (newLine.length() + content.length() > MessageEmbed.VALUE_MAX_LENGTH) break;

            content.append(newLine);
            rank++;
        }
        MessageEmbed embed = new EmbedBuilder()
                .setTitle("Leaderboard")
                .addField("Leaderboard", content.toString(), false)
                .setColor(new Color(59, 152, 0))
                .build();
        event.getHook().editOriginalEmbeds(embed).queue();
    }
}
