package org.pipeman.pipo;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class TopDonatorCache {
    private static List<String> topDonators = List.of();
    private static final CloseableHttpClient HTTP_CLIENT = HttpClients.createDefault();
    private static final Timer UPDATER = new Timer(true);

    private static void updateTopDonators() {
        HttpUriRequest request = RequestBuilder.get("https://ko-fi.com/kryeit/LeaderboardPartial")
                .addHeader("User-Agent", "Kryeit.com Top Supporters Leaderboard")
                .addHeader("Content-Length", "0")
                .build();

        try {
            CloseableHttpResponse response = HTTP_CLIENT.execute(request);
            String body = new String(response.getEntity().getContent().readAllBytes());
            response.close();

            Document doc = Jsoup.parse(body);
            topDonators = new ArrayList<>();
            doc.getElementsByClass("leaderboard-name").forEach(e -> {
                String name = e.text()
                        .replace("\n", "")
                        .trim();
                topDonators.add(name);
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    static {
        UPDATER.schedule(new TimerTask() {
            @Override
            public void run() {
                updateTopDonators();
            }
        }, 0, Duration.of(1, ChronoUnit.DAYS).toMillis());
    }

    public static void init() {

    }

    public static List<String> getTopDonators() {
        return topDonators;
    }
}
