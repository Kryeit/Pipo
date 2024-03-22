package org.pipeman.pipo.rest;

import io.javalin.http.ContentType;
import io.javalin.http.Context;
import io.javalin.http.Header;
import io.javalin.http.NotFoundResponse;
import org.pipeman.pipo.PlayerInformation;
import org.pipeman.pipo.Utils;

import java.util.Optional;

public class PlayerApi {
    public static void getPlayerInfo(Context ctx) {
        Optional<PlayerInformation> information = PlayerInformation.of(ctx.pathParam("player"));
        if (information.isEmpty()) {
            throw new NotFoundResponse("Player not found");
        }

        ctx.json(information.get());
    }

    public static void searchPlayerNames(Context ctx) {
        String query = ctx.queryParamAsClass("query", String.class).getOrDefault("");
        ctx.json(Utils.getNameSuggestions(query));
    }

    public static void getSkin(Context ctx) {
        ctx.header(Header.CONTENT_TYPE, ContentType.IMAGE_PNG.getMimeType());
        ctx.result(Utils.getSkin(ctx.pathParam("player")));
    }
}
