package org.pipeman.pipo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.mojang.authlib.GameProfile;
import net.minecraft.server.BannedPlayerEntry;
import net.minecraft.server.BannedPlayerList;
import org.pipeman.pipo.offline.Offlines;

import java.util.Date;
import java.util.Optional;
import java.util.UUID;

public record BanStatus(
        @JsonSerialize String playerName,
        @JsonSerialize String reason,
        @JsonSerialize Date creationDate,
        @JsonSerialize Date expiryDate
) {
    public static BanStatus ofPlayer(UUID uuid, String playerName) {
        BannedPlayerList banList = MinecraftServerSupplier.getServer().getPlayerManager().getUserBanList();
        BannedPlayerEntry banEntry = banList.get(new GameProfile(uuid, playerName));

        if (banEntry == null) return null;
        return new BanStatus(playerName, banEntry.getReason(), banEntry.getCreationDate(), banEntry.getExpiryDate());
    }

    public static BanStatus ofPlayer(String playerName) {
        Optional<UUID> uuid = Offlines.getUUIDbyName(playerName);
        return uuid.map(value -> ofPlayer(value, playerName)).orElse(null);
    }
}
