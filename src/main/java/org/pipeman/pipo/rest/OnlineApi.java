package org.pipeman.pipo.rest;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.javalin.http.Context;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.NotNull;
import org.pipeman.pipo.MinecraftServerSupplier;
import org.pipeman.pipo.Utils;
import org.pipeman.pipo.afk.AfkPlayer;

import java.util.Collections;
import java.util.List;

public class OnlineApi {
    public static void getOnlinePlayers(Context ctx) {
        List<OnlineEntry> names = Utils.map(MinecraftServerSupplier.getServer().getPlayerNames(), s -> new OnlineEntry(s, isAfk(s)));
        Collections.sort(names);

        ctx.json(names);
    }

    private static boolean isAfk(String name) {
        ServerPlayerEntity player = MinecraftServerSupplier.getServer().getPlayerManager().getPlayer(name);
        AfkPlayer afkPlayer = (AfkPlayer) player;

        return afkPlayer != null && afkPlayer.pipo$isAfk();
    }

    private record OnlineEntry(@JsonSerialize String playerName, @JsonSerialize boolean afk) implements Comparable<OnlineEntry> {
        @Override
        public int compareTo(@NotNull OnlineApi.OnlineEntry o) {
            return playerName.compareTo(o.playerName);
        }
    }
}
