package org.pipeman.pipo;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.pipeman.pipo.Leaderboard.LeaderboardEntry.Value;
import org.pipeman.pipo.offline.Offlines;
import org.pipeman.pipo.offline.OfflinesStats;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

public class Leaderboard {
    public static List<LeaderboardEntry> getLeaderboard(Order order, String valueKey, int limit) {
        List<LeaderboardEntry> leaderboard = getLeaderboard(order, valueKey);
        return leaderboard.subList(0, Math.min(limit, leaderboard.size()));
    }

    public static List<LeaderboardEntry> getLeaderboard(Order order, String valueKey, int limit, int offset) {
        List<LeaderboardEntry> leaderboard = getLeaderboard(order, valueKey);
        if (offset >= leaderboard.size()) return List.of();
        return leaderboard.subList(offset, Math.min(leaderboard.size(), limit + offset));
    }

    public static List<LeaderboardEntry> getLeaderboard(Order order, String valueKey) {
        List<LeaderboardEntry> list = new ArrayList<>();
        for (String name : Offlines.getPlayerNames()) {
            if (name != null) {
                list.add(new LeaderboardEntry(new Value(valueKey, name), name));
            }
        }
        list.sort(order.comparator());
        return list;
    }

    public static int getTotalCount() {
        int total = 0;
        for (String playerName : Offlines.getPlayerNames()) {
            if (playerName != null) total++;
        }
        return total;
    }

    public static int getRank(String playerName) {
        List<LeaderboardEntry> leaderboard = getLeaderboard(Order.DESC, "playtime");
        for (int i = 0; i < leaderboard.size(); i++)
            if (leaderboard.get(i).name().equals(playerName))
                return i + 1;

        return -1;
    }

    // This is so ugly
    public record LeaderboardEntry(@JsonUnwrapped Value value, @JsonSerialize String name) {
        public static class Value {
            private static final DateFormat DATE_FORMAT = new SimpleDateFormat("dd.MM.yyyy");

            private final long rawValue;
            @JsonProperty("value")
            private final String formatted;

            public Value(String name, String playerName) {
                rawValue = switch (name) {
                    case "playtime" -> Utils.getPlaytime(playerName);
                    case "last-played" -> PlayerInformation.of(playerName).map(PlayerInformation::lastSeen).orElse(0L);
                    case "distance-walked" -> statisticMapper("walk_one_cm", playerName);
                    case "deaths" -> statisticMapper("deaths", playerName);
                    case "mob-kills" -> statisticMapper("mob_kills", playerName);

                    default -> throw new IllegalStateException("Unexpected value: " + name);
                };

                formatted = switch (name) {
                    case "deaths", "mob-kills" -> rawValue + "";
                    case "playtime" -> Utils.round(rawValue / 3_600d, 1) + "h";
                    case "last-played" -> formatLastPlayed(rawValue);
                    case "distance-walked" -> String.format("%.1f km", rawValue / 100_000d);

                    default -> throw new IllegalStateException("Unexpected value: " + name);
                };
            }

            private static long statisticMapper(String stat, String playerName) {
                return OfflinesStats.getPlayerStat(stat, Offlines.getUUIDbyName(playerName).orElse(null));
            }

            private static String formatLastPlayed(long date) {
                int daysAgo = (int) Math.floorDiv(System.currentTimeMillis() - date, 86_400_000);
                String time = DATE_FORMAT.format(new Date(date));
                return "On " + time + " (" + daysAgo + " days ago)";
            }

            public long rawValue() {
                return rawValue;
            }

            public String formatted() {
                return formatted;
            }
        }
    }

    public enum Order {
        ASC(Comparator.comparingLong(o -> o.value().rawValue())),
        DESC(Comparator.comparingLong((LeaderboardEntry o) -> o.value().rawValue()).reversed());

        private final Comparator<LeaderboardEntry> comparator;

        Order(Comparator<LeaderboardEntry> comparator) {
            this.comparator = comparator;
        }

        public Comparator<LeaderboardEntry> comparator() {
            return comparator;
        }
    }
}
