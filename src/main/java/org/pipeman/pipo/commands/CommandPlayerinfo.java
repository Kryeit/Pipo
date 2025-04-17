package org.pipeman.pipo.commands;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.utils.FileUpload;
import org.pipeman.pipo.PlayerInformation;
import org.pipeman.pipo.Utils;
import org.pipeman.pipo.auth.UserApi;
import org.pipeman.pipo.offline.OfflinesStats;

import java.awt.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;

public class CommandPlayerinfo {
    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("dd.MM.yyyy");

    public static void handle(SlashCommandInteractionEvent event) {
        OptionMapping playerOption = event.getOption("playername");
        String playerName = playerOption == null ? null : playerOption.getAsString();
        playerName = UserApi.getNameByUUID(UserApi.getUUIDbyName(playerName));

        if (playerName == null || playerName.isEmpty()) {

            // Check if the user has linked their Minecraft account to their Discord account
            playerName = UserApi.getNameByUUID(Utils.getPlayerLinked(event.getMember()));

            if (playerName == null || playerName.isEmpty()) {
                event.reply("You need to specify a player name or link your Minecraft account to your Discord account.").setEphemeral(true).queue();
                return;
            }
        }

        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setTitle(Utils.escapeName(playerName), "https://kryeit.com/@" + playerName);
        embedBuilder.setColor(new Color(59, 152, 0));

        event.deferReply().queue();
        Optional<PlayerInformation> information = PlayerInformation.of(playerName);

        embedBuilder.addField(
                "Last Seen",
                information.map(inf -> {
                    if (inf.online()) {
                        return "Currently online";
                    } else {
                        long lastSeen = inf.lastSeen();
                        int daysAgo = (int) Math.floorDiv(System.currentTimeMillis() - lastSeen, 86_400_000);
                        String time = DATE_FORMAT.format(new Date(lastSeen));
                        return "On " + time + " (" + daysAgo + " days ago)";
                    }
                }).orElse("Not found. Check spelling and use suggestions provided when typing the command."),
                false
        );

        String finalPlayerName = playerName;
        information.ifPresent(inf -> {
            embedBuilder.addField(
                    "Playtime",
                    inf.playtime() / 3_600 + " hours",
                    false
            );
            embedBuilder.addField("Rank", Utils.ordinal(inf.rank()), true);

            inf.totalClaimBlocks().ifPresent(blocks ->
                    embedBuilder.addField(
                            "Total claimblocks",
                            blocks + " blocks",
                            false
                    )
            );
            embedBuilder.addField(
                    "Linked Discord account",
                    inf.linkedDiscordAccount().map(l -> "<@!" + l + ">").orElse("Not linked yet"),
                    true
            );

            embedBuilder.addField(
                    "Other statistics",
                    String.format("%.1f km walked, %d deaths, %d mobs killed",
                            OfflinesStats.getPlayerStat("walk_one_cm", UserApi.getUUIDbyName(finalPlayerName)) / 100_000d,
                            OfflinesStats.getPlayerStat("deaths", UserApi.getUUIDbyName(finalPlayerName)),
                            OfflinesStats.getPlayerStat("mob_kills", UserApi.getUUIDbyName(finalPlayerName))
                    ),
                    false
            );
        });


        String filename = playerName.hashCode() + ".png";
        embedBuilder.setThumbnail("attachment://" + filename);
        event.getHook().sendFiles(FileUpload.fromData(Utils.getHeadSkin(playerName), filename))
                .setEmbeds(embedBuilder.build())
                .queue();
    }
}
