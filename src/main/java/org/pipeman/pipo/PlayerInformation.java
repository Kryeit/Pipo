package org.pipeman.pipo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.pipeman.pipo.compat.GriefDefenderImpl;
import org.pipeman.pipo.offline.Offlines;
import org.pipeman.pipo.rest.OptionalSerializer;

import java.util.Optional;
import java.util.UUID;

public record PlayerInformation(
        @JsonSerialize int rank,
        @JsonSerialize long playtime,
        @JsonSerialize long lastSeen,
        @JsonSerialize boolean online,
        @JsonSerialize(using = OptionalSerializer.class) Optional<Integer> totalClaimBlocks,
        @JsonSerialize BanStatus banStatus
) {

    public static Optional<PlayerInformation> of(String playerName) {
        Optional<UUID> optionalUUID = Offlines.getUUIDbyName(playerName);
        if (optionalUUID.isEmpty()) return Optional.empty();
        UUID uuid = optionalUUID.get();

        Optional<String> nameByUUID = Offlines.getNameByUUID(uuid);
        if (nameByUUID.isEmpty()) return Optional.empty();
        playerName = nameByUUID.get();

        return Optional.of(new PlayerInformation(
                Leaderboard.getRank(playerName),
                Utils.getPlaytime(uuid),
                Utils.getLastPlayed(uuid),
                Utils.isOnline(playerName),
                GriefDefenderImpl.isAvailable() ?
                        Optional.of(GriefDefenderImpl.getClaimBlocks(uuid)) :
                        Optional.empty(),
                BanStatus.ofPlayer(uuid, playerName)
        ));
    }
}
