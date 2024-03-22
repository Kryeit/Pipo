package org.pipeman.pipo.rest;

import io.javalin.http.BadRequestResponse;
import io.javalin.http.Context;
import org.pipeman.pipo.Leaderboard;
import org.pipeman.pipo.Leaderboard.Order;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class LeaderboardApi {
    private static final Set<String> LEADERBOARD_ATTRIBUTES = Set.of("playtime", "last-played", "distance-walked", "deaths", "mob-kills");

    public static void getLeaderboard(Context ctx) {
        Order order = getOrder(ctx);
        String orderKey = Optional.ofNullable(ctx.queryParam("order-by")).orElse("playtime");
        int offset = ctx.queryParamAsClass("offset", Integer.class).getOrDefault(0);
        int limit = ctx.queryParamAsClass("limit", Integer.class).getOrDefault(10);

        if (!LEADERBOARD_ATTRIBUTES.contains(orderKey)) {
            throw new BadRequestResponse("Invalid order key: " + orderKey);
        }

        ctx.json(Map.of(
                "total-count", Leaderboard.getTotalCount(),
                "leaderboard", Leaderboard.getLeaderboard(order, orderKey, limit, offset)
        ));
    }

    private static Order getOrder(Context ctx) {
        String orderParam = ctx.queryParam("sort-direction");
        if (orderParam == null) return Order.ASC;

        return switch (orderParam) {
            case "ASC" -> Order.ASC;
            case "DESC" -> Order.DESC;
            default -> throw new BadRequestResponse("Unknown order: " + orderParam);
        };
    }
}
