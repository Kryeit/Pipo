package org.pipeman.pipo.rest;

import io.javalin.http.ContentType;
import io.javalin.http.Context;
import io.javalin.http.Header;
import io.javalin.http.NotFoundResponse;
import org.pipeman.pipo.PlayerInformation;
import org.pipeman.pipo.Utils;

import java.util.Optional;
import java.util.UUID;

public class PlayerApi {
    public static void getPlayerInfo(Context ctx) {
        String player = ctx.pathParam("player");
        Optional<UUID> uuid = parseUUID(player);


        Optional<PlayerInformation> information = uuid.isPresent()
                ? PlayerInformation.of(uuid.get())
                : PlayerInformation.of(player);

        ctx.json(information.orElseThrow(() -> new NotFoundResponse("Player not found")));
    }

    public static void searchPlayerNames(Context ctx) {
        String query = ctx.queryParamAsClass("query", String.class).getOrDefault("");
        ctx.json(Utils.getNameSuggestions(query));
    }

    public static void getHeadSkin(Context ctx) {
        ctx.header(Header.CONTENT_TYPE, ContentType.IMAGE_PNG.getMimeType());
        ctx.result(Utils.getHeadSkin(ctx.pathParam("player")));
    }

    public static void getSkin(Context ctx) {
        ctx.header(Header.CONTENT_TYPE, ContentType.IMAGE_PNG.getMimeType());
        ctx.result(Utils.getSkin(ctx.pathParam("player")));
    }

    private static Optional<UUID> parseUUID(String s) {
        try {
            return Optional.of(UUID.fromString(s));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }
}
