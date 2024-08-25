package org.pipeman.pipo.commands;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.Heightmap;
import net.minecraft.world.chunk.ChunkManager;
import net.minecraft.world.chunk.ChunkStatus;
import org.pipeman.pipo.MinecraftServerSupplier;

import java.util.HashSet;
import java.util.Set;

public class CommandTpToAgua {
    private static final Set<String> playersToTeleportOnJoin = new HashSet<>();

    public static void handle(SlashCommandInteractionEvent event) {
        MinecraftServer server = MinecraftServerSupplier.getServer();
        String playername = event.getOption("playername", OptionMapping::getAsString);

        ServerPlayerEntity player = server.getPlayerManager().getPlayer(playername);
        if (player == null) {
            playersToTeleportOnJoin.add(playername);
            event.reply(playername + " will be teleported to Agua the next time they join")
                    .setEphemeral(true)
                    .queue();
        } else {
            teleport(player, server);
            event.reply(playername + " has been teleported to Agua")
                    .setEphemeral(true)
                    .queue();
        }
    }

    public static void teleport(ServerPlayerEntity player, MinecraftServer server) {
        int x = 0;
        int z = 0;
        ServerWorld world = server.getOverworld();
        ChunkManager chunkManager = world.getChunkManager();
        chunkManager.getChunk(0, 0, ChunkStatus.FULL, true);

        player.teleport(world, x + 0.5, world.getTopY(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, x, z) + 1, z + 0.5, player.getYaw(), player.getPitch());
    }

    public static void teleportIfNecessary(ServerPlayerEntity player, MinecraftServer server) {
        if (playersToTeleportOnJoin.contains(player.getEntityName())) {
            teleport(player, server);
            playersToTeleportOnJoin.remove(player.getEntityName());
        }
    }
}
