package org.pipeman.pipo.commands;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import org.pipeman.pipo.Pipo;
import org.pipeman.pipo.commands.minecraft.CommandLinkDiscord;

public class CommandVerify {
    public static void handle(SlashCommandInteractionEvent event) {
        //noinspection DataFlowIssue (Option is required)
        int code = event.getOption("code", OptionMapping::getAsInt);

        if (CommandLinkDiscord.codes.containsKey(code)) {
            Pipo.getInstance().discordRegistry.linkPlayerToDiscord(CommandLinkDiscord.codes.get(code), String.valueOf(event.getMember().getIdLong()));

            event.reply("Your account has been linked to this UUID: " + CommandLinkDiscord.codes.get(code))
                    .setEphemeral(true)
                    .queue();
            CommandLinkDiscord.codes.remove(code);
        } else {
            event.reply("Invalid code")
                    .setEphemeral(true)
                    .queue();
        }
    }
}
