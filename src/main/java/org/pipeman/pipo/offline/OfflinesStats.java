package org.pipeman.pipo.offline;

import org.pipeman.pipo.PostgresDatabase;

import java.util.UUID;

public class OfflinesStats {

    public static long getPlayerStat(String stat, UUID player) {
        return getPlayerStat("minecraft:custom", stat, player);
    }

    public static long getPlayerStat(String category, String stat, UUID player) {
        return PostgresDatabase.getJdbi().withHandle(handle -> {
            String query = "SELECT stats -> :category ->> :stat FROM users WHERE uuid = :player";

            return handle.createQuery(query)
                    .bind("category", category)
                    .bind("stat", stat.toLowerCase())
                    .bind("player", player)
                    .mapTo(String.class)
                    .findOne()
                    .map(Long::parseLong)
                    .orElse(0L);
        });
    }
}