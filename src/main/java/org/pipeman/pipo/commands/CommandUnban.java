package org.pipeman.pipo.commands;

import com.mojang.authlib.GameProfile;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.minecraft.server.BannedPlayerList;
import net.minecraft.server.MinecraftServer;
import org.pipeman.pipo.MinecraftServerSupplier;

import java.util.Optional;

public class CommandUnban {
    public static void handle(SlashCommandInteractionEvent event) {
        MinecraftServer server = MinecraftServerSupplier.getServer();
        BannedPlayerList banList = server.getPlayerManager().getUserBanList();
        String playerName = event.getOption("banned-playername", OptionMapping::getAsString);

        Optional<GameProfile> profile = server.getUserCache().findByName(playerName);
        if (profile.isEmpty()) {
            reply(event, "Player does not exist");
            return;
        }

        if (!banList.contains(profile.get())) {
            reply(event, "Player is not banned");
            return;
        }

        banList.remove(profile.get());
        reply(event, profile.get().getName() + " has been unbanned");
    }

    private static void reply(SlashCommandInteractionEvent event, String message) {
        event.reply(message)
                .setEphemeral(true)
                .queue();
    }
}
