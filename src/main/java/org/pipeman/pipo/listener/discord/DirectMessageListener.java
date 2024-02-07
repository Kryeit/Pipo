package org.pipeman.pipo.listener.discord;

import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.pipeman.pipo.Pipo;
import org.pipeman.pipo.commands.minecraft.CommandLinkDiscord;

public class DirectMessageListener extends ListenerAdapter {
    @Override
    public void onMessageReceived(MessageReceivedEvent event) {


        if (event.getAuthor().isBot()) return; // Ignore messages from other bots
        if (!event.isFromType(ChannelType.PRIVATE)) return;

        String message = event.getMessage().getContentRaw();

        int code = Integer.parseInt(message);


        if (CommandLinkDiscord.codes.containsKey(code)) {
            Pipo.getInstance().minecraftToDiscord.addElement(CommandLinkDiscord.codes.get(code), String.valueOf(event.getAuthor().getIdLong()));

            event.getChannel().sendMessage("Your account has been linked to this UUID: " + CommandLinkDiscord.codes.get(code)).queue();
            CommandLinkDiscord.codes.remove(code);
        }
    }
}
