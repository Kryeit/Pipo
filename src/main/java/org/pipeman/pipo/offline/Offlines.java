package org.pipeman.pipo.offline;

import com.mojang.authlib.GameProfile;
import net.minecraft.server.network.ServerPlayerEntity;
import org.pipeman.pipo.MinecraftServerSupplier;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Logger;

public class Offlines {

    private static final Logger LOGGER = Logger.getLogger(Offlines.class.getName());

    public static Optional<UUID> getUUIDbyName(String name) {
        return Optional.ofNullable(getPlayerByName(name))
                .map(ServerPlayerEntity::getUuid)
                .or(() -> Optional.ofNullable(MinecraftServerSupplier.getServer().getUserCache())
                        .flatMap(cache -> cache.findByName(name).map(GameProfile::getId)));
    }

    public static Optional<String> getNameByUUID(UUID id) {
        return Optional.ofNullable(getPlayerById(id))
                .map(player -> player.getName().getString())
                .or(() -> Optional.ofNullable(MinecraftServerSupplier.getServer().getUserCache())
                        .flatMap(cache -> cache.getByUuid(id).map(GameProfile::getName)));
    }

    public static List<String> getPlayerNames() {
        List<String> players = new ArrayList<>();
        Path playerDataDirectory = Path.of("world/playerdata");

        try {
            Files.walk(playerDataDirectory, 1)
                    .filter(Files::isRegularFile)
                    .map(Path::getFileName)
                    .map(Path::toString)
                    .filter(fileName -> fileName.endsWith(".dat"))
                    .map(fileName -> fileName.substring(0, fileName.length() - 4))
                    .forEach(fileName -> {
                        try {
                            UUID id = UUID.fromString(fileName);
                            getNameByUUID(id).ifPresent(players::add);
                        } catch (IllegalArgumentException e) {
                            LOGGER.warning("Invalid UUID in playerdata filename: " + fileName);
                        }
                    });
        } catch (Exception e) {
            LOGGER.severe("Failed to list player data files: " + e.getMessage());
        }

        return players;
    }

    private static ServerPlayerEntity getPlayerByName(String name) {
        return MinecraftServerSupplier.getServer().getPlayerManager().getPlayer(name);
    }

    private static ServerPlayerEntity getPlayerById(UUID id) {
        return MinecraftServerSupplier.getServer().getPlayerManager().getPlayer(id);
    }
}
