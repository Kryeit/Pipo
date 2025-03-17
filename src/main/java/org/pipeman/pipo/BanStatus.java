package org.pipeman.pipo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.kryeit.idler.MinecraftServerSupplier;
import com.mojang.authlib.GameProfile;
import net.minecraft.server.BannedPlayerEntry;
import net.minecraft.server.BannedPlayerList;
import org.pipeman.pipo.auth.UserApi;

import java.util.Date;
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
        UUID uuid = UserApi.getUUIDbyName(playerName);
        return ofPlayer(uuid, playerName);
    }
}
