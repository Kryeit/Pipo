package org.pipeman.pipo.rest;

import io.javalin.http.Context;
import net.minecraft.server.BannedPlayerList;
import org.pipeman.pipo.BanStatus;
import org.pipeman.pipo.MinecraftServerSupplier;
import org.pipeman.pipo.Utils;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class BanApi {
    public static void getBans(Context ctx) {
        BannedPlayerList banList = MinecraftServerSupplier.getServer().getPlayerManager().getUserBanList();
        List<String> bannedPlayers = Arrays.stream(banList.getNames())
                .filter(Objects::nonNull)
                .toList();
        ctx.json(Utils.map(bannedPlayers, BanStatus::ofPlayer));
    }
}
