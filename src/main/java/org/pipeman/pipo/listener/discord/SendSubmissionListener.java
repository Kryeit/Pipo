package org.pipeman.pipo.listener.discord;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.List;

public class SendSubmissionListener extends ListenerAdapter {

    public static final String PHOTO_CONTEST_CHANNEL = "1245982225384603710";

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (!event.getChannel().getId().equals(PHOTO_CONTEST_CHANNEL)) return;

        String userId = event.getAuthor().getId();
        List<Message> messages = event.getChannel().getHistory().retrievePast(200).complete();
        for (Message message : messages) {
            if (message.getAuthor().getId().equals(userId)) {
                event.getMessage().delete().queue();
                return;
            }
        }

        event.getMessage().addReaction(Emoji.fromUnicode("👍")).queue(); // Thumbs up
        event.getMessage().addReaction(Emoji.fromUnicode("👎")).queue(); // Thumbs down
        event.getMessage().addReaction(Emoji.fromUnicode("🚫")).queue(); // Cancel
    }
}
