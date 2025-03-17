package org.pipeman.pipo.auth;


import org.pipeman.pipo.Database;

import java.sql.Timestamp;
import java.util.UUID;

public class UserApi {

    public static Timestamp getLastSeen(UUID uuid) {
        return Database.getJdbi().withHandle(handle ->
                handle.createQuery("SELECT last_seen FROM users WHERE uuid = :uuid")
                        .bind("uuid", uuid)
                        .mapTo(Timestamp.class)
                        .findOne()
                        .orElse(null)
        );
    }

    public static Iterable<UUID> getKnownPlayers() {
        return Database.getJdbi().withHandle(handle ->
                handle.createQuery("SELECT uuid FROM users")
                        .mapTo(UUID.class)
                        .list()
        );
    }

    public static UUID getUUIDbyName(String playerName) {
        return Database.getJdbi().withHandle(handle ->
                handle.createQuery("SELECT uuid FROM users WHERE username = :username")
                        .bind("username", playerName)
                        .mapTo(UUID.class)
                        .findOne()
                        .orElse(null)
        );
    }

    public static String getNameByUUID(UUID uuid) {
        return Database.getJdbi().withHandle(handle ->
                handle.createQuery("SELECT username FROM users WHERE uuid = :uuid")
                        .bind("uuid", uuid)
                        .mapTo(String.class)
                        .findOne()
                        .orElse(null)
        );
    }
}
