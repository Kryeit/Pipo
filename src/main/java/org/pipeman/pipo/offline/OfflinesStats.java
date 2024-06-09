package org.pipeman.pipo.offline;

import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.UUID;

public class OfflinesStats {

    public static long getPlayerStat(String stat, UUID player) {
        File playerDataDirectory = new File("world/stats/");
        File[] statDataFiles = playerDataDirectory.listFiles((dir, name) -> name.endsWith(".json"));

        if (statDataFiles == null) return 0;

        for (File playerDataFile : statDataFiles) {
            try {
                UUID id = UUID.fromString(playerDataFile.getName().replace(".json", ""));

                if (!player.equals(id)) continue;

                String jsonContent = new String(Files.readAllBytes(playerDataFile.toPath()));
                JSONObject statData = new JSONObject(jsonContent);

                JSONObject customStats = statData.getJSONObject("stats").getJSONObject("minecraft:custom");
                return customStats.optLong("minecraft:" + stat.toLowerCase(), 0);
            } catch (IllegalArgumentException | IOException e) {
                System.err.println("Error processing file " + playerDataFile.getName() + ": " + e.getMessage());
            } catch (Exception e) {
                System.err.println("Unexpected error: " + e.getMessage());
            }
        }
        return 0;
    }
}

