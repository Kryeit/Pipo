package org.pipeman.pipo.offline;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.authlib.GameProfile;
import net.minecraft.server.network.ServerPlayerEntity;
import org.pipeman.pipo.MinecraftServerSupplier;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
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
        String url = "https://api.mojang.com/users/profiles/minecraft/" + name;

        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);

            int responseCode = connection.getResponseCode();
            if (responseCode == 200) {
                try (InputStreamReader reader = new InputStreamReader(connection.getInputStream())) {
                    JsonObject jsonObject = JsonParser.parseReader(reader).getAsJsonObject();
                    String id = jsonObject.get("id").getAsString();
                    return Optional.of(UUID.fromString(
                            id.replaceFirst(
                                    "(\\p{XDigit}{8})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}+)",
                                    "$1-$2-$3-$4-$5"
                            )
                    ));
                }
            } else if (responseCode == 204) {
                return Optional.empty(); // No content, player name not found
            } else {
                throw new RuntimeException("HTTP error code: " + responseCode);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }

    public static Optional<String> getNameByUUID(UUID id) {
        ServerPlayerEntity player = MinecraftServerSupplier.getServer().getPlayerManager().getPlayer(id);
        if (player != null) {
            return Optional.of(player.getName().getString());
        }

        String uuidString = id.toString().replace("-", ""); // Remove hyphens from UUID
        String url = "https://sessionserver.mojang.com/session/minecraft/profile/" + uuidString;

        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);

            int responseCode = connection.getResponseCode();
            if (responseCode == 200) {
                try (InputStreamReader reader = new InputStreamReader(connection.getInputStream())) {
                    JsonObject jsonObject = JsonParser.parseReader(reader).getAsJsonObject();
                    return Optional.of(jsonObject.get("name").getAsString());
                }
            } else if (responseCode == 204) {
                return Optional.empty();
            } else {
                throw new RuntimeException("HTTP error code: " + responseCode);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Optional.empty();
        }
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
