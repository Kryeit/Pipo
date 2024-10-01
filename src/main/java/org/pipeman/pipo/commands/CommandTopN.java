package org.pipeman.pipo.commands;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import org.pipeman.pipo.Leaderboard;
import org.pipeman.pipo.Leaderboard.LeaderboardEntry;
import org.pipeman.pipo.Leaderboard.Order;
import org.pipeman.pipo.TopDonatorCache;
import org.pipeman.pipo.Utils;

import java.awt.*;
import java.text.MessageFormat;
import java.util.List;

public class CommandTopN {
    public static void handle(SlashCommandInteractionEvent event) {
        int limit = event.getOption("limit", 10, OptionMapping::getAsInt);
        int offset = event.getOption("offset", 0, OptionMapping::getAsInt);
        boolean sortDescending = event.getOption("sort-direction", "descending", OptionMapping::getAsString).equals("descending");
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

        event.deferReply().queue();

        MessageEmbed embed = new EmbedBuilder()
                .setTitle("Leaderboard")
                .addField("Leaderboard", buildLeaderboardContent(leaderboard, offset + 1), false)
                .setColor(new Color(59, 152, 0))
                .build();
        event.getHook().editOriginalEmbeds(embed).queue();
    }

    public static void handleTop10(SlashCommandInteractionEvent event) {
        event.deferReply().queue();

        List<LeaderboardEntry> entries = Leaderboard.getLeaderboard(Order.DESC, "playtime", 10, 0);
        List<String> topDonators = TopDonatorCache.getTopDonators();

        MessageEmbed embed = new EmbedBuilder()
                .setTitle("Leaderboard")
                .addField("Leaderboard", buildLeaderboardContent(entries, 1), true)
                .addField("Top Supporters", buildTopDonatorContent(topDonators), true)
                .addField("You can donate here", "https://ko-fi.com/kryeit", false)
                .setColor(new Color(59, 152, 0))
                .build();
        event.getHook().editOriginalEmbeds(embed).queue();
    }

    private static String buildLeaderboardContent(List<LeaderboardEntry> entries, int startRank) {
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
        return content.toString();
    }

    private static String buildTopDonatorContent(List<String> entries) {
        StringBuilder content = new StringBuilder();
        int rank = 1;
        for (String name : entries) {
            String newLine = MessageFormat.format(
                    "**{0}:** {1}\n",
                    rank, Utils.escapeName(name)
            );
            if (newLine.length() + content.length() > MessageEmbed.VALUE_MAX_LENGTH) break;

            content.append(newLine);
            rank++;
        }
        return content.toString();
    }
}
