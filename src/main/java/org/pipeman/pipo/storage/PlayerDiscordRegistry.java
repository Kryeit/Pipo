package org.pipeman.pipo.storage;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class PlayerDiscordRegistry {

    private final File registryFile;
    private final Map<UUID, String> playerDiscordMap = new HashMap<>();

    public PlayerDiscordRegistry(String directory, String fileName) throws IOException {
        Files.createDirectories(Paths.get(directory));
        this.registryFile = new File(directory, fileName);
        if (!registryFile.exists()) {
            registryFile.createNewFile();
        }
        loadRegistry();
    }

    private void loadRegistry() {
        try (BufferedReader reader = new BufferedReader(new FileReader(registryFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("=", 2);
                if (parts.length == 2) {
                    playerDiscordMap.put(UUID.fromString(parts[0]), parts[1]);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void linkPlayerToDiscord(UUID playerUuid, String discordId) {
        playerDiscordMap.put(playerUuid, discordId);
        saveRegistry();
    }

    public void unlinkPlayer(UUID playerUuid) {
        playerDiscordMap.remove(playerUuid);
        saveRegistry();
    }

    public String getDiscordId(UUID playerUuid) {
        return playerDiscordMap.get(playerUuid);
    }

    public Optional<Long> getDiscordIdLong(UUID playerUuid) {
        String value = playerDiscordMap.get(playerUuid);
        return Optional.ofNullable(value == null ? null : Long.parseLong(value));
    }

    public boolean hasPlayer(UUID playerUuid) {
        return playerDiscordMap.containsKey(playerUuid);
    }

    public boolean hasDiscordId(String discordId) {
        return playerDiscordMap.containsValue(discordId);
    }

    public Optional<UUID> getPlayerUuid(String discordId) {
        return playerDiscordMap.entrySet().stream()
                .filter(entry -> entry.getValue().equals(discordId))
                .map(Map.Entry::getKey)
                .findFirst();
    }

    private void saveRegistry() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(registryFile, false))) {
            for (Map.Entry<UUID, String> entry : playerDiscordMap.entrySet()) {
                writer.println(entry.getKey().toString() + "=" + entry.getValue());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
