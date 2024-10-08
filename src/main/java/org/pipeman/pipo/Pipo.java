package org.pipeman.pipo;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.ISnowflake;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import org.pipeman.pipo.commands.CommandListener;
import org.pipeman.pipo.commands.minecraft.*;
import org.pipeman.pipo.listener.discord.ButtonInteractionListener;
import org.pipeman.pipo.listener.discord.DirectMessageListener;
import org.pipeman.pipo.listener.discord.DownloadModsListener;
import org.pipeman.pipo.listener.discord.SendSubmissionListener;
import org.pipeman.pipo.listener.minecraft.PlayerLogin;
import org.pipeman.pipo.listener.minecraft.PlayerQuit;
import org.pipeman.pipo.listener.minecraft.ServerStarted;
import org.pipeman.pipo.rest.RestApiServer;
import org.pipeman.pipo.storage.LastTimePlayed;
import org.pipeman.pipo.storage.PlayerDiscordRegistry;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public final class Pipo implements DedicatedServerModInitializer {
    public static JDA JDA;
    public final static String KRYEIT_GUILD = "910626990468497439";
    public LastTimePlayed lastTimePlayed;
    public PlayerDiscordRegistry discordRegistry;
    private static Pipo instance;

    public static HashMap<UUID, Long> lastActiveTime = new HashMap<>();
    private RestApiServer restApiServer;

    @Override
    public void onInitializeServer() {
        instance = this;
        CommandLinkDiscord.codes = new HashMap<>();
        restApiServer = new RestApiServer();

        try {
            lastTimePlayed = new LastTimePlayed("mods/pipo/last_time_played");
            discordRegistry = new PlayerDiscordRegistry("mods/pipo", "discord_registry.properties");

            JDA = JDABuilder.createDefault(readToken())
                    .setActivity(Activity.watching("0 players"))
                    .enableIntents(GatewayIntent.MESSAGE_CONTENT, GatewayIntent.GUILD_MEMBERS)
                    .build();

            JDA.addEventListener(new CommandListener());
            JDA.addEventListener(new DownloadModsListener());
            JDA.addEventListener(new DirectMessageListener());
            JDA.addEventListener(new ButtonInteractionListener());
            JDA.addEventListener(new SendSubmissionListener());

            JDA.awaitReady();
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }

        Guild guild = JDA.getGuildById(KRYEIT_GUILD);
        if (guild != null) {
            guild.upsertCommand("voyage", "Creates a voyage embed")
                    .addOption(OptionType.STRING, "name", "The company/city's name", true)
                    .addOption(OptionType.STRING, "title", "The title of the embed", true)
                    .addOption(OptionType.STRING, "secondary-title", "Smaller title", true)
                    .addOption(OptionType.STRING, "description", "Long description, use \\n to do a line jump", true)
                    .addOption(OptionType.STRING, "logo-url", "Photo of the logo, use a URL", true)
                    .addOption(OptionType.STRING, "image-url", "A photo that will be showcased in big size, use a URL", false)
                    .queue();

            guild.upsertCommand("changelog", "Sends a changelog")
                    .addOption(OptionType.STRING, "author", "The author of the Changelog", true)
                    .addOption(OptionType.STRING, "changelog", "The changelog paragraph", true)
                    .addOption(OptionType.STRING, "version", "Version number. Example: 3.7", true)
                    .addOption(OptionType.BOOLEAN, "update", "Does the version number increase? (Big update)", true)
                    .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR))
                    .queue();

            guild.upsertCommand("kofi", "Sends the donation Link")
                    .queue();
            guild.upsertCommand("donate", "Sends the donation Link")
                    .queue();

            guild.upsertCommand("vote", "Changes your nickname to your Minecraft name")
                    .queue();

            guild.upsertCommand("ban", "Bans a player")
                    .addOption(OptionType.STRING, "playername", "The player's name", true, true)
                    .addOption(OptionType.STRING, "reason", "The reason for the ban", false)
                    .addOption(OptionType.STRING, "duration", "The duration of the ban", false, true)
                    .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.MODERATE_MEMBERS))
                    .queue();

            guild.upsertCommand("unban", "Unbans a player")
                    .addOption(OptionType.STRING, "banned-playername", "The player's name", true, true)
                    .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.MODERATE_MEMBERS))
                    .queue();

            guild.upsertCommand("tp-to-agua", "Teleports the player to Agua")
                    .addOption(OptionType.STRING, "playername", "The player's name", true, true)
                    .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.MODERATE_MEMBERS))
                    .queue();

            guild.upsertCommand("nickname", "Changes your nickname to your Minecraft name")
                    .queue();

            guild.upsertCommand("close", "Closes a ticket")
                    .queue();

            guild.upsertCommand("ticket", "Creates a ticket")
                    .queue();

            guild.upsertCommand("verify", "Connect your Minecraft account to your Discord account by typing /linkdiscord in the Minecraft chat")
                    .addOption(OptionType.INTEGER, "code", "The code you received in Minecraft", true)
                    .queue();

            guild.upsertCommand("tps", "Returns current TPS")
                    .queue();

            guild.upsertCommand("online", "Returns currently online players")
                    .queue();

            guild.upsertCommand("playerinfo", "Returns playtime information about the provided user")
                    .addOption(OptionType.STRING, "playername", "The player's name", true, true)
                    .queue();

            guild.upsertCommand("top-10", "Returns a top-10 list of the most active users")
                    .queue();

            guild.upsertCommand("mods", "Returns instructions on how to join the server")
                    .queue();

            guild.upsertCommand("top-n", "Returns a list of the most active users. Limit and offset can be specified")
                    .addOption(OptionType.INTEGER, "limit", "Limit of elements to return", true)
                    .addOption(OptionType.INTEGER, "offset", "Offset of the returned elements in the list", true)

                    .addOptions(new OptionData(OptionType.STRING, "order-by", "Order by statistic (The playtime is used if omitted)", false)
                            .addChoice("playtime", "playtime")
                            .addChoice("distance-walked", "distance-walked")
                            .addChoice("deaths", "deaths")
                            .addChoice("mob-kills", "mob-kills")
                            .addChoice("potatoes", "potatoes")
                    )
                    .addOptions(new OptionData(OptionType.STRING, "sort-direction", "Sorting direction (descending is used if omitted)", false)
                            .addChoice("ascending", "ascending")
                            .addChoice("descending", "descending")
                    )
                    .queue();
        } else {
            System.out.println("Guild is null!");
        }

        registerEvents();
        registerCommands();
        registerDisableEvent();

        scheduleTimers();
        TopDonatorCache.init();
    }

    public void registerCommands() {
        CommandRegistrationCallback.EVENT.register((dispatcher, dedicatedServer, commandFunction) -> {
            CommandLinkDiscord.register(dispatcher);
            CommandUnlinkDiscord.register(dispatcher);
            CommandDiscord.register(dispatcher);
            CommandPotatoLeaderboard.register(dispatcher);
            CommandWeb.register(dispatcher);
        });
    }

    public void registerDisableEvent() {
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            JDA.shutdown();
            restApiServer.stop();
            restApiServer = null;
            JDA = null;
        });
    }

    public void registerEvents() {
        ServerPlayConnectionEvents.JOIN.register(new PlayerLogin());
        ServerPlayConnectionEvents.DISCONNECT.register(new PlayerQuit());
        ServerLifecycleEvents.SERVER_STARTED.register(new ServerStarted());
    }

    public static boolean isMe(ISnowflake user) {
        return user != null && user.getIdLong() == JDA.getSelfUser().getIdLong();
    }

    public static Pipo getInstance() {
        return instance;
    }

    public void scheduleTimers() {
        ScheduledExecutorService executor = JDA.getRateLimitPool();
        executor.scheduleAtFixedRate(new Autorole(JDA.getRoleById(Autorole.KRYEITOR)), 5, 5, TimeUnit.MINUTES);
        executor.scheduleAtFixedRate(new Autorole(JDA.getRoleById(Autorole.COLLABORATOR)), 5, 5, TimeUnit.MINUTES);
        executor.scheduleAtFixedRate(new Autorole(JDA.getRoleById(Autorole.BOOSTER)), 5, 5, TimeUnit.MINUTES);
    }

    private String readToken() throws IOException {
        try (InputStream in = this.getClass().getResourceAsStream("/secret.txt")) {
            if (in == null) {
                throw new FileNotFoundException("Resource not found: secret.txt");
            }
            return new String(in.readAllBytes()).trim();
        }
    }

    public static String readClickHouseKey() {
        try (InputStream in = Pipo.class.getResourceAsStream("/clickhouse-secret.txt")) {
            if (in == null) {
                throw new FileNotFoundException("Resource not found: clickhouse-secret.txt");
            }
            return new String(in.readAllBytes()).trim();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
