package org.pipeman.pipo.offline;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.authlib.GameProfile;
import net.minecraft.server.network.ServerPlayerEntity;
import org.pipeman.pipo.MinecraftServerSupplier;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
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
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.mojang.com/users/profiles/minecraft/" + name))
                .build();

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                JsonObject jsonObject = JsonParser.parseString(response.body()).getAsJsonObject();
                String uuidString = jsonObject.get("id").getAsString();
                // Insert hyphens into the UUID because the Mojang API returns it without them
                return Optional.of(UUID.fromString(uuidString.replaceFirst(
                        "(\\p{XDigit}{8})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}+)",
                        "$1-$2-$3-$4-$5"
                )));
            }
        } catch (IOException | InterruptedException ignored) {}

        return Optional.empty();
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
