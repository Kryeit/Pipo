package org.pipeman.pipo.commands;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.pipeman.pipo.MinecraftServerSupplier;
import com.mojang.authlib.GameProfile;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.minecraft.server.BannedPlayerEntry;
import net.minecraft.server.BannedPlayerList;
import net.minecraft.server.MinecraftServer;
import org.pipeman.pipo.DurationParser;

import java.text.ParseException;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;

public class CommandBan {
    public static void handle(SlashCommandInteractionEvent event) {
        MinecraftServer server = MinecraftServerSupplier.getServer();
        BannedPlayerList banList = server.getPlayerManager().getUserBanList();
        String playerName = event.getOption("playername", OptionMapping::getAsString);
        String reason = event.getOption("reason", null, OptionMapping::getAsString);
        String duration = event.getOption("duration", null, OptionMapping::getAsString);

        Optional<GameProfile> profile = server.getUserCache().findByName(playerName);
        if (profile.isEmpty()) {
            reply(event, "Player does not exist");
            return;
        }

        if (banList.contains(profile.get())) {
            reply(event, "Player is already banned");
            return;
        }

        Duration parsedDuration = null;
        if (duration != null) {
            try {
                parsedDuration = DurationParser.parseDuration(duration);
            } catch (ParseException e) {
                reply(event, e.getMessage() + " at " + e.getErrorOffset());
                return;
            }
        }

        banList.add(new BannedPlayerEntry(
                profile.get(),
                new Date(),
                event.getUser().getName() + " via Discord",
                parsedDuration == null ? null : Date.from(Instant.now().plus(parsedDuration)),
                reason
        ));

        ServerPlayerEntity player = server.getPlayerManager().getPlayer(profile.get().getId());

        if (player != null) {
            player.networkHandler.disconnect(Text.literal(
                    "You have been banned from the server"
            ));
        }

        reply(event, profile.get().getName() + " has been banned");
    }

    private static void reply(SlashCommandInteractionEvent event, String message) {
        event.reply(message)
                .setEphemeral(true)
                .queue();
    }
}
