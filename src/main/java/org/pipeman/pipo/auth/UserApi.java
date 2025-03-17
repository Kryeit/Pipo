package org.pipeman.pipo.auth;

import org.pipeman.pipo.PostgresDatabase;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class UserApi {
    private static final Map<UUID, String> playerCache = new ConcurrentHashMap<>();
    private static final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private static boolean isInitialized = false;

    public static void initialize() {
        if (!isInitialized) {
            refreshCache();
            scheduler.scheduleAtFixedRate(UserApi::refreshCache, 5, 5, TimeUnit.MINUTES);
            isInitialized = true;
        }
    }

    public static void shutdown() {
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(10, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
        playerCache.clear();
        isInitialized = false;
    }

    public static Map<UUID, String> getKnownPlayersWithNames() {
        initialize();
        return new HashMap<>(playerCache);
    }

    public static void refreshCache() {
        Map<UUID, String> players = PostgresDatabase.getJdbi().withHandle(handle ->
                handle.createQuery("SELECT uuid, username FROM users")
                        .reduceRows(new HashMap<>(), (map, row) -> {
                            UUID uuid = row.getColumn("uuid", UUID.class);
                            String username = row.getColumn("username", String.class);
                            map.put(uuid, username);
                            return map;
                        })
        );
        playerCache.clear();
        playerCache.putAll(players);
    }

    public static Timestamp getLastSeen(UUID uuid) {
        return PostgresDatabase.getJdbi().withHandle(handle ->
                handle.createQuery("SELECT last_seen FROM users WHERE uuid = :uuid")
                        .bind("uuid", uuid)
                        .mapTo(Timestamp.class)
                        .findOne()
                        .orElse(null)
        );
    }

    public static List<UUID> getKnownPlayers() {
        initialize();
        return List.copyOf(playerCache.keySet());
    }

    public static UUID getUUIDbyName(String playerName) {
        initialize();

        for (Map.Entry<UUID, String> entry : playerCache.entrySet()) {
            if (entry.getValue().equalsIgnoreCase(playerName)) {
                return entry.getKey();
            }
        }

        UUID uuid = PostgresDatabase.getJdbi().withHandle(handle ->
                handle.createQuery("SELECT uuid FROM users WHERE username = :username")
                        .bind("username", playerName)
                        .mapTo(UUID.class)
                        .findOne()
                        .orElse(null)
        );

        if (uuid != null) {
            playerCache.put(uuid, playerName);
        }

        return uuid;
    }

    public static String getNameByUUID(UUID uuid) {
        initialize();

        String cachedName = playerCache.get(uuid);
        if (cachedName != null) {
            return cachedName;
        }

        String username = PostgresDatabase.getJdbi().withHandle(handle ->
                handle.createQuery("SELECT username FROM users WHERE uuid = :uuid")
                        .bind("uuid", uuid)
                        .mapTo(String.class)
                        .findOne()
                        .orElse(null)
        );

        if (username != null) {
            playerCache.put(uuid, username);
        }

        return username;
    }
}