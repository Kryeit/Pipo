package org.pipeman.pipo.offline;

import com.mojang.authlib.GameProfile;
import org.pipeman.pipo.MinecraftServerSupplier;
import org.pipeman.pipo.Utils;

import java.io.File;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class Offlines {
    public static List<UUID> getKnownPlayers() {
        String[] playerdataFiles = new File("world/playerdata").list((dir, name) -> name.endsWith(".dat"));
        if (playerdataFiles == null) return List.of();

        return Utils.map(playerdataFiles, name -> UUID.fromString(name.substring(0, name.lastIndexOf('.'))));
    }

    public static Optional<UUID> getUUIDbyName(String name) {
        return MinecraftServerSupplier.getServer().getUserCache().findByName(name).map(GameProfile::getId);
    }

    public static Optional<String> getNameByUUID(UUID id) {
        return MinecraftServerSupplier.getServer().getUserCache().getByUuid(id).map(GameProfile::getName);
    }
}
