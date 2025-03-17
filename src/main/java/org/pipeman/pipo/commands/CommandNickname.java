package org.pipeman.pipo.commands;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.pipeman.pipo.Pipo;
import org.pipeman.pipo.auth.UserApi;

import java.util.UUID;

// changes the nickname of the user using the command to the minecraft player name if he is linked
public class CommandNickname {

    public static void handle(SlashCommandInteractionEvent event) {

        UUID uuid = Pipo.getInstance().discordRegistry.getPlayerUuid(String.valueOf(event.getUser().getIdLong())).orElse(null);

        if (uuid == null) {
            event.reply("You are not linked to any minecraft account, use /linkdiscord inside the Minecraft server").setEphemeral(true).queue();
            return;
        }

        String nickname = UserApi.getNameByUUID(uuid);
        Guild guild =  event.getGuild();

        if (nickname == null) return;
        if (guild == null) return;

        String currentNickname = guild.retrieveMember(event.getUser()).complete().getNickname();

        if (currentNickname != null && currentNickname.equals(nickname)) {
            event.reply("Your nickname is already set to your minecraft name, so it will be toggled back to your original nickname. Use again /nickname to get your player name as a discord nickname").setEphemeral(true).queue();
            guild.retrieveMember(event.getUser()).complete().modifyNickname(null).queue();
            return;
        }


        event.reply("Your nickname has been set to your minecraft name").setEphemeral(true).queue();
        guild.modifyNickname(event.getMember(), nickname).queue();

    }
}
