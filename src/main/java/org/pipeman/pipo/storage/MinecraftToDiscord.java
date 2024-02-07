package org.pipeman.pipo.storage;

import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.Serializer;

import java.util.UUID;
import java.util.concurrent.ConcurrentMap;

public class MinecraftToDiscord {
    private ConcurrentMap<UUID, String> map;

    public MinecraftToDiscord() {
        DB db = DBMaker
                .fileDB("mods/pipo/discord_linking.db")
                .fileMmapEnable()
                .make();

        map = db
                .hashMap("discord_linking", Serializer.UUID, Serializer.STRING)
                .createOrOpen();
    }

    public void addElement(UUID playerID, String memberID) {
        map.put(playerID, memberID);
    }

    public void removeElement(UUID playerID) {
        map.remove(playerID);
    }

    public boolean hasElement(UUID playerID) {
        return map.containsKey(playerID);
    }

    public String getElement(UUID playerID) {
        return map.get(playerID);
    }
}
