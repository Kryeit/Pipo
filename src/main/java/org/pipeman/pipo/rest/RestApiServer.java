package org.pipeman.pipo.rest;

import io.javalin.Javalin;

import static io.javalin.apibuilder.ApiBuilder.get;
import static io.javalin.apibuilder.ApiBuilder.path;

public class RestApiServer {
    private final Javalin javalin;

    public RestApiServer() {
        this.javalin = Javalin.create(config -> {
            config.showJavalinBanner = false;

            config.router.apiBuilder(() -> {
                path("api", () -> {
                    path("players", () -> {
                        path("{player}", () -> {
                            get(PlayerApi::getPlayerInfo);
                            get("head", PlayerApi::getHeadSkin);
                            get("skin", PlayerApi::getSkin);
                        });
                        get("", PlayerApi::searchPlayerNames);
                    });
                    get("bans", BanApi::getBans);
                    get("leaderboard", LeaderboardApi::getLeaderboard);
                    get("online", OnlineApi::getOnlinePlayers);
                });
            });
        });
        new Thread(() -> javalin.start(4001)).start();
    }

    public void stop() {
        javalin.stop();
    }
}