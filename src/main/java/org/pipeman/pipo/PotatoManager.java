package org.pipeman.pipo;

import org.pipeman.pipo.Leaderboard.LeaderboardEntry;
import org.pipeman.pipo.Leaderboard.LeaderboardEntry.Value;
import org.pipeman.pipo.Leaderboard.Rank;
import org.pipeman.pipo.auth.UserApi;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class PotatoManager {
    private static final Executor EXECUTOR = Executors.newFixedThreadPool(4);

    public static void storePotatoes(UUID player, int count) {
        EXECUTOR.execute(() -> {
            ClickhouseDatabase.getJdbi().useHandle(h -> h.createUpdate("""
                            INSERT INTO potatoes (player, amount)
                            VALUES (:player, :count)
                            """)
                    .bind("count", count)
                    .bind("player", player)
                    .execute());
        });
    }

    public static List<LeaderboardEntry> getLeaderboard(Leaderboard.Order order, int offset, int limit) {
        return ClickhouseDatabase.getJdbi().withHandle(h -> h.createQuery("""
                        SELECT sum(amount) AS sum, player
                        FROM potatoes
                        GROUP BY player
                        ORDER BY if(:sort_order = 'ASC', sum, -sum)
                        LIMIT :limit OFFSET :offset
                        """)
                .bind("sort_order", order)
                .bind("offset", offset)
                .bind("limit", limit)
                .map((rs, ctx) -> {
                    String name = UserApi.getNameByUUID(rs.getObject("player", UUID.class));
                    return new LeaderboardEntry(new Value(rs.getInt("sum")), name);
                })
                .list());
    }

    public static Optional<Rank> getRank(UUID player) {
        return ClickhouseDatabase.getJdbi().withHandle(h -> h.createQuery("""
                        SELECT rank, sum
                        FROM (
                                 SELECT player,
                                        sum(amount)                           AS sum,
                                        dense_rank() OVER (ORDER BY sum DESC) AS rank
                                 FROM potatoes
                                 GROUP BY player
                             ) AS ranked_players
                        WHERE player = :player
                        """)
                .bind("player", player)
                .map((rs, ctx) -> new Rank(rs.getInt("rank"), new Value(rs.getInt("sum"))))
                .findFirst());
    }

    public static int getTotalCount() {
        return ClickhouseDatabase.getJdbi().withHandle(h -> h.createQuery("""
                        SELECT DISTINCT count(*)
                        FROM potatoes
                        """)
                .mapTo(Integer.class)
                .findFirst())
                .orElse(0);
    }
}
