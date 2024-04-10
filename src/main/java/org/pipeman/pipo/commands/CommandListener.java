package org.pipeman.pipo.commands;

import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import org.pipeman.pipo.Utils;

public class CommandListener extends ListenerAdapter {
    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        switch (event.getName()) {
            case "tps" -> CommandTPS.handle(event);
            case "online" -> CommandOnline.handle(event);
            case "playerinfo" -> CommandPlayerinfo.handle(event);
            case "top-10" -> CommandTopN.handleTop10(event);
            case "top-n" -> CommandTopN.handle(event);
            case "mods" -> CommandMods.handle(event);
            case "verify" -> CommandVerify.handle(event);
            case "ticket" -> CommandTicket.handle(event);
            case "close" -> CommandCloseTicket.handle(event);
        }
    }

    @Override
    public void onCommandAutoCompleteInteraction(CommandAutoCompleteInteractionEvent event) {
        if (!event.getName().equals("playerinfo")) return;
        String value = event.getOption("playername", "", OptionMapping::getAsString);
        event.replyChoiceStrings(Utils.getNameSuggestions(value)).queue();
    }
}
