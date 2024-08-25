package org.pipeman.pipo.commands.minecraft;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.function.Supplier;

public class CommandWeb {

    private static final String LOCAL_IP = getLocalIpAddress();
    private static final String VERIFY_URL = "http://" + LOCAL_IP + ":6969/api/v1/auth/verify";
    private static final String DELETE_URL = "http://" + LOCAL_IP + ":6969/api/v1/auth/delete";

    public static int generateCode(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        ServerPlayerEntity player = source.getPlayer();

        if (player == null) {
            Supplier<Text> message = () -> Text.of("Can't execute from console");
            source.sendFeedback(message, false);
            return 0;
        }

        String uuid = player.getUuidAsString();
        try {
            String response = sendPostRequest(VERIFY_URL, uuid);
            source.sendFeedback(() -> Text.of("Verification code generated successfully: " + response), false);
        } catch (Exception e) {
            source.sendFeedback(() -> Text.of("Failed to generate verification code: " + e.getMessage()), false);
            e.printStackTrace();
            return 0;
        }

        return Command.SINGLE_SUCCESS;
    }

    public static int delete(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        ServerPlayerEntity player = source.getPlayer();

        if (player == null) {
            Supplier<Text> message = () -> Text.of("Can't execute from console");
            source.sendFeedback(message, false);
            return 0;
        }

        String uuid = player.getUuidAsString();
        try {
            sendDeleteRequest(DELETE_URL, uuid);
            source.sendFeedback(() -> Text.of("Account deleted successfully"), false);
        } catch (Exception e) {
            source.sendFeedback(() -> Text.of("Failed to delete account: " + e.getMessage()), false);
            e.printStackTrace();
            return 0;
        }

        return Command.SINGLE_SUCCESS;
    }

    private static void sendDeleteRequest(String endpoint, String uuid) throws Exception {
        URL url = new URL(endpoint);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("DELETE");
        conn.setRequestProperty("Content-Type", "application/json; utf-8");
        conn.setRequestProperty("Accept", "application/json");
        conn.setDoOutput(true);

        String jsonInputString = "{\"uuid\": \"" + uuid + "\"}";

        try (OutputStream os = conn.getOutputStream()) {
            byte[] input = jsonInputString.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        if (conn.getResponseCode() != 200) {
            throw new RuntimeException("Failed to delete account: HTTP error code " + conn.getResponseCode());
        }
    }

    private static String sendPostRequest(String endpoint, String uuid) throws Exception {
        URL url = new URL(endpoint);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json; utf-8");
        conn.setRequestProperty("Accept", "application/json");
        conn.setDoOutput(true);

        String jsonInputString = "{\"uuid\": \"" + uuid + "\"}";

        try (OutputStream os = conn.getOutputStream()) {
            byte[] input = jsonInputString.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
            StringBuilder response = new StringBuilder();
            String responseLine;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }
            return response.toString();
        }
    }

    private static String getLocalIpAddress() {
        try {
            InetAddress localHost = InetAddress.getLocalHost();
            return localHost.getHostAddress();
        } catch (Exception e) {
            throw new RuntimeException("Failed to get local IP address", e);
        }
    }

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("web")
                .then(CommandManager.literal("verify")
                        .executes(CommandWeb::generateCode)
                )
                .then(CommandManager.literal("deleteaccount")
                        .executes(CommandWeb::delete)
                )
        );
    }
}
