package org.pipeman.pipo.rest;

import io.javalin.http.Context;
import net.minecraft.server.BannedPlayerList;
import org.pipeman.pipo.BanStatus;
import org.pipeman.pipo.MinecraftServerSupplier;
import org.pipeman.pipo.Utils;

public class BanApi {
    public static void getBans(Context ctx) {
        BannedPlayerList banList = MinecraftServerSupplier.getServer().getPlayerManager().getUserBanList();
        ctx.json(Utils.map(banList.getNames(), BanStatus::ofPlayer));
    }
}
