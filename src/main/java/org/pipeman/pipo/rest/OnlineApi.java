package org.pipeman.pipo.rest;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.javalin.http.Context;
import io.javalin.websocket.WsCloseContext;
import io.javalin.websocket.WsConnectContext;
import io.javalin.websocket.WsContext;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.NotNull;
import org.pipeman.pipo.MinecraftServerSupplier;
import org.pipeman.pipo.Utils;
import org.pipeman.pipo.afk.AfkPlayer;

import java.util.Collections;
import java.util.List;
import java.util.Queue;
import java.util.Timer;
import java.util.concurrent.ConcurrentLinkedQueue;

public class OnlineApi {
    private static final Timer broadcastTimer = new Timer("Broadcast-Timer", true);
    private static final Queue<WsContext> connections = new ConcurrentLinkedQueue<>();

    public static void getOnlinePlayers(Context ctx) {
        ctx.json(getOnlinePlayers());
    }

    public static void wsConnect(WsConnectContext ctx) {
        ctx.enableAutomaticPings();
        connections.add(ctx);
    }

    public static void wsClose(WsCloseContext ctx) {
        connections.remove(ctx);
    }

    public static void broadcastChange() {
        broadcastTimer.schedule(Utils.timerTask(() -> {
            List<OnlineEntry> players = getOnlinePlayers();
            for (WsContext connection : connections) {
                connection.send(players);
            }
        }), 1000);
    }

    private static List<OnlineEntry> getOnlinePlayers() {
        List<OnlineEntry> entries = Utils.map(MinecraftServerSupplier.getServer().getPlayerNames(), s -> new OnlineEntry(s, isAfk(s)));
        Collections.sort(entries);
        return entries;
    }

    private static boolean isAfk(String name) {
        ServerPlayerEntity player = MinecraftServerSupplier.getServer().getPlayerManager().getPlayer(name);
        AfkPlayer afkPlayer = (AfkPlayer) player;

        return afkPlayer != null && afkPlayer.pipo$isAfk();
    }

    private record OnlineEntry(@JsonSerialize String playerName,
                               @JsonSerialize boolean afk) implements Comparable<OnlineEntry> {
        @Override
        public int compareTo(@NotNull OnlineApi.OnlineEntry o) {
            return playerName.compareTo(o.playerName);
        }
    }
}
