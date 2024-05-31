package org.pipeman.pipo.listener.discord;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageReaction;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
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

        boolean hasAttachment = !event.getMessage().getAttachments().isEmpty();
        boolean containsLink = event.getMessage().getContentRaw().matches(".*(http[s]?://www\\.|http[s]?://|www\\.).*");

        if (hasAttachment || containsLink) {
            event.getMessage().addReaction(Emoji.fromUnicode("👍")).queue(); // Thumbs up
            event.getMessage().addReaction(Emoji.fromUnicode("👎")).queue(); // Thumbs down
            event.getMessage().addReaction(Emoji.fromUnicode("🚫")).queue(); // Cancel
        } else {
            event.getMessage().delete().queue();
        }
    }

    @Override
    public void onMessageReactionAdd(MessageReactionAddEvent event) {
        if (!event.getChannel().getId().equals(PHOTO_CONTEST_CHANNEL) || event.getUser().isBot()) return;

        Message message = event.retrieveMessage().complete();
        String userId = event.getUserId();
        String emojiAdded = event.getReaction().getEmoji().asUnicode().getAsCodepoints();

        if (!emojiAdded.equals("U+1F44D") && !emojiAdded.equals("U+1F44E")) return; // Ignore non thumbs up/down reactions

        // Check for existing opposite reactions
        List<MessageReaction> reactions = message.getReactions();
        for (MessageReaction reaction : reactions) {
            if (reaction.retrieveUsers().complete().stream().anyMatch(user -> user.getId().equals(userId))) {
                if ((emojiAdded.equals("U+1F44D") && reaction.getEmoji().asUnicode().getAsCodepoints().equals("U+1F44E")) ||
                        (emojiAdded.equals("U+1F44E") && reaction.getEmoji().asUnicode().getAsCodepoints().equals("U+1F44D"))) {
                    reaction.removeReaction(event.getUser()).queue();
                }
            }
        }
    }
}

