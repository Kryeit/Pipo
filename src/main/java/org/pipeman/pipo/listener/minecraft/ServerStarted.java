package org.pipeman.pipo.listener.minecraft;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.MinecraftServer;

import java.awt.*;

import static org.pipeman.pipo.Pipo.JDA;

public class ServerStarted implements ServerLifecycleEvents.ServerStarted {

    private static final String rolePinged = "1237771841850834954";
    @Override
    public void onServerStarted(MinecraftServer server) {
        EmbedBuilder builder = new EmbedBuilder()
                .setColor(new Color(59, 152, 0))
                .setTitle("Kryeit.com");

        Role role = JDA.getRoleById(rolePinged);

        builder.addField(
                "The server has been started!",
                "You may log in and play",
                false);
        builder.build();

        String commandsChannelID = "1059547652229963908";
        TextChannel commandsChannel = JDA.getTextChannelById(commandsChannelID);
        if (commandsChannel == null) return;
        if (role != null) {
            commandsChannel.sendMessage(role.getAsMention()).queue();
        }
        commandsChannel.sendMessageEmbeds(builder.build()).queue();
    }
}
