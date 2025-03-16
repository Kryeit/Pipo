package org.pipeman.pipo.listener.minecraft;

import net.dv8tion.jda.api.entities.Activity;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import org.pipeman.pipo.Pipo;
import org.pipeman.pipo.Utils;
import org.pipeman.pipo.rest.OnlineApi;
import org.pipeman.pipo.tps.Lag;

import java.io.IOException;
import java.text.DecimalFormat;

public class PlayerQuit implements ServerPlayConnectionEvents.Disconnect {
    private static final DecimalFormat format = new DecimalFormat("#.##");

    @Override
    public void onPlayDisconnect(ServerPlayNetworkHandler handler, MinecraftServer server) {
        OnlineApi.broadcastChange();

        if (Utils.getOnlinePlayersSize() - 1 == 1) {
            Pipo.JDA.getPresence().setActivity(Activity.watching("1 player - " + format.format(Lag.getTPS()) + " TPS"));
        } else {
            Pipo.JDA.getPresence().setActivity(Activity.watching((Utils.getOnlinePlayersSize() - 1) + " players - " + format.format(Lag.getTPS()) + " TPS"));
        }
    }
}
