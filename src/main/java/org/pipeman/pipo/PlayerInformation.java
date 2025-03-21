package org.pipeman.pipo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.pipeman.pipo.auth.UserApi;
import org.pipeman.pipo.compat.GriefDefenderImpl;
import org.pipeman.pipo.rest.OptionalSerializer;

import java.util.Optional;
import java.util.UUID;

public record PlayerInformation(
        @JsonSerialize String name,
        @JsonSerialize UUID uuid,
        @JsonSerialize int rank,
        @JsonSerialize long playtime,
        @JsonSerialize long lastSeen,
        @JsonSerialize boolean online,
        @JsonSerialize boolean afk,
        @JsonSerialize(using = OptionalSerializer.class) Optional<Integer> totalClaimBlocks,
        @JsonSerialize BanStatus banStatus,
        @JsonSerialize Optional<Long> linkedDiscordAccount
) {

    public static Optional<PlayerInformation> of(String playerName) {
        UUID uuid = UserApi.getUUIDbyName(playerName);
        if (uuid == null) return Optional.empty();

        String name = UserApi.getNameByUUID(uuid);
        return of(uuid, name);
    }

    public static Optional<PlayerInformation> of(UUID uuid) {
        String name = UserApi.getNameByUUID(uuid);
        if (name == null) return Optional.empty();

        return of(uuid, name);
    }

    public static Optional<PlayerInformation> of(UUID uuid, String playerName) {
        return Optional.of(new PlayerInformation(
                playerName,
                uuid,
                Leaderboard.getRank(playerName),
                Utils.getPlaytime(uuid),
                Utils.getLastPlayed(uuid),
                Utils.isOnline(playerName),
                Utils.isAFK(playerName),
                GriefDefenderImpl.isAvailable() ?
                        Optional.of(GriefDefenderImpl.getClaimBlocks(uuid)) :
                        Optional.empty(),
                BanStatus.ofPlayer(uuid, playerName),
                Pipo.getInstance().discordRegistry.getDiscordIdLong(uuid)
        ));
    }
}
