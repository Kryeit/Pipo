package org.pipeman.pipo;

import org.pipeman.pipo.MinecraftServerSupplier;
import com.kryeit.idler.afk.AfkPlayer;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.node.Node;
import net.minecraft.server.network.ServerPlayerEntity;
import org.json.JSONObject;
import org.pipeman.pipo.auth.UserApi;
import org.pipeman.pipo.offline.OfflinesStats;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public class  Utils {
    private static final Logger LOGGER = LoggerFactory.getLogger(Utils.class);
    private static final HttpClient CLIENT = HttpClient.newHttpClient();

    public static byte[] getHeadSkin(String name) {
        try {
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(getSkin(name)));
            BufferedImage output = new BufferedImage(8, 8, Image.SCALE_FAST);

            copyRect(image, output, 8, 8, 8, 8, 0, 0);
            copyRect(image, output, 40, 8, 8, 8, 0, 0);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ImageIO.write(scaleImage(output, 32), "png", out);

            return out.toByteArray();
        } catch (Exception e) {
            LOGGER.error("Failed to download skin", e);
            return new byte[0];
        }
    }

    public static byte[] getSkin(String name) {
        try {
            UUID uuid = UserApi.getUUIDbyName(name);
            if (uuid == null) return new byte[0];

            HttpRequest request = HttpRequest.newBuilder(URI.create("https://sessionserver.mojang.com/session/minecraft/profile/" + uuid)).build();

            String body = CLIENT.send(request, HttpResponse.BodyHandlers.ofString()).body();
            JSONObject data = new JSONObject(body);
            byte[] decode = Base64.getDecoder().decode(data.getJSONArray("properties").getJSONObject(0).getString("value"));
            String url = new JSONObject(new String(decode)).getJSONObject("textures").getJSONObject("SKIN").getString("url");

            try (InputStream stream = new URL(url).openStream()) {
                return stream.readAllBytes();
            }
        } catch (Exception e) {
            LOGGER.error("Failed to download skin", e);
            return new byte[0];
        }
    }

    private static RenderedImage scaleImage(BufferedImage in, int scale) {
        BufferedImage out = new BufferedImage(in.getWidth() * scale, in.getHeight() * scale,
                BufferedImage.TYPE_INT_RGB);

        for (int x = 0; x < in.getWidth(); x++) {
            for (int y = 0; y < in.getHeight(); y++) {
                drawSquare(out, in.getRGB(x, y), x * scale, y * scale, scale);
            }
        }
        return out;
    }

    private static void drawSquare(BufferedImage image, int color, int x, int y, int size) {
        for (int xI = 0; xI < size; xI++) {
            for (int yI = 0; yI < size; yI++) {
                image.setRGB(xI + x, yI + y, color);
            }
        }
    }

    private static void copyRect(BufferedImage in, BufferedImage out, int x, int y, int width, int height, int destX, int destY) {
        for (int xI = 0; xI < width; xI++) {
            for (int yI = 0; yI < height; yI++) {
                if (in == null || out == null) continue;
                Color newColor = new Color(in.getRGB(xI + x, yI + y), true);

                if (newColor.getAlpha() == 0) continue;

                Color oldColor = new Color(out.getRGB(xI + destX, yI + destY), true);

                float alpha = newColor.getAlpha() / 255.0f;
                int r = (int) (newColor.getRed() * alpha + oldColor.getRed() * (1 - alpha));
                int g = (int) (newColor.getGreen() * alpha + oldColor.getGreen() * (1 - alpha));
                int b = (int) (newColor.getBlue() * alpha + oldColor.getBlue() * (1 - alpha));

                out.setRGB(xI + destX, yI + destY, new Color(r, g, b).getRGB());
            }
        }
    }

    public static double round(double value, int precision) {
        int scale = (int) Math.pow(10, precision);
        return (double) Math.round(value * scale) / scale;
    }

    public static String ordinal(int i) {
        int mod100 = i % 100;
        int mod10 = i % 10;
        if (mod10 == 1 && mod100 != 11) return i + "st";
        else if (mod10 == 2 && mod100 != 12) return i + "nd";
        else if (mod10 == 3 && mod100 != 13) return i + "rd";
        return i + "th";
    }

    public static <T, R> List<T> map(Collection<R> list, Function<R, T> mappingFunction) {
        List<T> out = new ArrayList<>();
        for (R r : list) {
            out.add(mappingFunction.apply(r));
        }
        return out;
    }

    public static <T, R> List<T> map(R[] list, Function<R, T> mappingFunction) {
        List<T> out = new ArrayList<>();
        for (R r : list) {
            out.add(mappingFunction.apply(r));
        }
        return out;
    }

    public static MessageEmbed createErrorEmbed(String error) {
        return new EmbedBuilder()
                .setTitle("Error")
                .addField("Description", error, false)
                .setColor(new Color(59, 152, 0))
                .build();
    }

    public static long getPlaytime(String name) {
        UUID id = UserApi.getUUIDbyName(name);
        return getPlaytime(id);
    }

    public static long getPlaytime(UUID uuid) {
        //    long afkTime = AFK_PLUS.getPlayer(player).getTotalTimeAFK() / 1000;
//        Stat<Identifier> stat = Stats.CUSTOM.getOrCreateStat(Stats.PLAY_TIME);
        long playtime = OfflinesStats.getPlayerStat("minecraft:play_time", uuid) / 20;
        //    return Math.max(0, playtime - afkTime);
        return Math.max(0, playtime);
    }

    public static long getLastPlayed(UUID uuid) {
        return UserApi.getLastSeen(uuid).getTime();
    }

    public static boolean isOnline(String name) {
        return List.of(MinecraftServerSupplier.getServer().getPlayerNames()).contains(name);
    }

    public static boolean isAFK(String playerName) {
        ServerPlayerEntity player = MinecraftServerSupplier.getServer().getPlayerManager().getPlayer(playerName);
        AfkPlayer afkPlayer = (AfkPlayer) player;

        return afkPlayer != null && afkPlayer.idler$isAfk();
    }

    public static int getOnlinePlayersSize() {
        return MinecraftServerSupplier.getServer().getCurrentPlayerCount();
    }

    public static UUID getMinecraftId(Member member) {
        return Pipo.getInstance().discordRegistry.getPlayerUuid(member.getId()).orElse(null);
    }

    public static CompletableFuture<Boolean> isPlayerOnGroup(UUID who, String group) {
        return LuckPermsProvider.get().getUserManager().loadUser(who)
                .thenApplyAsync(user -> {
                    Collection<Group> inheritedGroups = user.getInheritedGroups(user.getQueryOptions());
                    return inheritedGroups.stream().anyMatch(g -> g.getName().equals(group));
                });
    }

    public static void addGroup(UUID id, String permission) {
        LuckPermsProvider.get().getUserManager().modifyUser(id, user ->
                user.data().add(Node.builder("group." + permission).build())
        );
    }

    public static void removeGroup(UUID id, String permission) {
        LuckPermsProvider.get().getUserManager().modifyUser(id, user ->
                user.data().remove(Node.builder("group." + permission).build())
        );
    }

    public static UUID getPlayerLinked(Member member) {
        return Pipo.getInstance().discordRegistry.getPlayerUuid(member.getId()).orElse(null);
    }

    public static String escapeName(String name) {
        return name.replace("_", "\\_");
    }

    public static List<String> getNameSuggestions(String input) {
        String lcInput = input.toLowerCase();
        List<String> players = new ArrayList<>();
        for (String name : UserApi.getKnownPlayersWithNames().values()) {
            if (players.size() >= 5) break;

            if (name != null && name.toLowerCase().contains(lcInput) && !players.contains(name)) {
                players.add(name);
            }
        }
        return players;
    }

    public static List<String> getBannedNameSuggestions(String input) {
        input = input.toLowerCase();
        List<String> players = new ArrayList<>();
        for (String name : MinecraftServerSupplier.getServer().getPlayerManager().getUserBanList().getNames()) {
            if (players.size() >= 5) break;
            if (name != null && name.toLowerCase().contains(input) && !players.contains(name)) {
                players.add(name);
            }
        }
        return players;
    }

    public static TimerTask timerTask(Runnable runnable) {
        return new TimerTask() {
            @Override
            public void run() {
                runnable.run();
            }
        };
    }
}
