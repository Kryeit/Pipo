package org.pipeman.pipo.commands;

import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.AutoCompleteQuery;
import org.pipeman.pipo.DurationParser;
import org.pipeman.pipo.Utils;

import java.util.List;

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
            case "nickname" -> CommandNickname.handle(event);
            case "ban" -> CommandBan.handle(event);
            case "unban" -> CommandUnban.handle(event);
            case "tp-to-agua" -> CommandTpToAgua.handle(event);
            case "kofi", "donate" -> CommandKofi.handle(event);
            case "vote" -> CommandVote.handle(event);
            case "changelog" -> CommandChangelog.handle(event);
            case "voyage" -> CommandVoyage.handle(event);
            case "toggle-ping" -> CommandTogglePing.handle(event);
        }
    }

    @Override
    public void onCommandAutoCompleteInteraction(CommandAutoCompleteInteractionEvent event) {
        AutoCompleteQuery focusedOption = event.getFocusedOption();
        String value = event.getFocusedOption().getValue();

        List<String> suggestions = switch (focusedOption.getName()) {
            case "playername" -> Utils.getNameSuggestions(value);
            case "banned-playername" -> Utils.getBannedNameSuggestions(value);
            case "duration" -> DurationParser.suggestDuration(value);
            default -> List.of();
        };

        event.replyChoiceStrings(suggestions).queue();
    }
}
