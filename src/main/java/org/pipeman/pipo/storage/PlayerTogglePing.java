package org.pipeman.pipo.storage;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class PlayerTogglePing {

    private final File registryFile;
    private final Map<UUID, Boolean> playerTogglePingMap = new HashMap<>();

    public PlayerTogglePing(String directory, String fileName) throws IOException {
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
                    playerTogglePingMap.put(UUID.fromString(parts[0]), Boolean.parseBoolean(parts[1]));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void togglePing(UUID playerUuid, boolean isEnabled) {
        playerTogglePingMap.put(playerUuid, isEnabled);
        saveRegistry();
    }

    public void removePlayer(UUID playerUuid) {
        playerTogglePingMap.remove(playerUuid);
        saveRegistry();
    }

    public Optional<Boolean> isPingEnabled(UUID playerUuid) {
        return Optional.ofNullable(playerTogglePingMap.get(playerUuid));
    }

    public boolean hasPlayer(UUID playerUuid) {
        return playerTogglePingMap.containsKey(playerUuid);
    }

    private void saveRegistry() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(registryFile, false))) {
            for (Map.Entry<UUID, Boolean> entry : playerTogglePingMap.entrySet()) {
                writer.println(entry.getKey().toString() + "=" + entry.getValue());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}