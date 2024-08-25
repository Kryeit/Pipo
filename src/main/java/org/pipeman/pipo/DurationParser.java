package org.pipeman.pipo;

import java.text.ParseException;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DurationParser {
    private static final Pattern PARSER = Pattern.compile("(?<amount>\\d+)(?<unit>[A-Za-z]+)?");
    private static final Map<String, TemporalUnit> timeUnits = Map.of(
            "s", ChronoUnit.SECONDS,
            "m", ChronoUnit.MINUTES,
            "h", ChronoUnit.HOURS,
            "d", ChronoUnit.DAYS
    );

    public static List<String> suggestDuration(String input) {
        String[] split = input.split(" ");
        for (int i = 0; i < split.length; i++) {
            Matcher matcher = PARSER.matcher(split[i]);
            if (!matcher.matches()) return List.of();

            if (tryParseInt(matcher.group("amount")).isEmpty()) {
                return List.of();
            }

            String unit = matcher.group("unit");
            if (unit != null && !timeUnits.containsKey(unit)) {
                return List.of();
            }

            if (i == split.length - 1 && unit == null) {
                return prepareSuggestions(input, timeUnits.keySet());
            }
        }

        return List.of();
    }

    public static Duration parseDuration(String input) throws ParseException {
        String[] split = input.split(" ");
        int index = 0;
        Duration total = Duration.ZERO;

        for (String s : split) {
            Matcher matcher = PARSER.matcher(s);
            if (!matcher.find()) throw new ParseException("Missing unit", index);

            Optional<Integer> amount = tryParseInt(matcher.group("amount"));
            if (amount.isEmpty()) throw new ParseException("Expected an integer", index);

            int tmpIndex = index + matcher.group("amount").length();

            String unit = matcher.group("unit");
            if (unit == null) {
                throw new ParseException("Missing unit", tmpIndex);
            }

            if (!timeUnits.containsKey(unit)) throw new ParseException("Unknown unit. Valid values are: " + timeUnits.keySet(), tmpIndex);
            total = total.plus(Duration.of(amount.get(), timeUnits.get(unit)));

            index += s.length() + 1;
        }

        return total;
    }

    private static List<String> prepareSuggestions(String input, Collection<String> possibleAdditions) {
        List<String> list = new ArrayList<>(possibleAdditions.size());
        for (String s : possibleAdditions) {
            list.add(input + s);
        }
        return list;
    }

    private static Optional<Integer> tryParseInt(String s) {
        try {
            return Optional.of(Integer.parseInt(s));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }
}
