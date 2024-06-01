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
        String messageId = event.getMessageId();
        List<Message> messages = event.getChannel().getHistory().retrievePast(100).complete();
        if (messages != null && !messages.isEmpty()) {
            for (Message message : messages) {
                if (message.getAuthor().getId().equals(userId) && !message.getId().equals(messageId)) {
                    event.getMessage().delete().queue();
                    return;
                }
            }
        }

        boolean hasAttachment = !event.getMessage().getAttachments().isEmpty();
        boolean containsLink = event.getMessage().getContentRaw().matches(".*(http[s]?://www\\.|http[s]?://|www\\.).*");

        if (hasAttachment || containsLink) {
            event.getMessage().addReaction(Emoji.fromUnicode("👍")).queue(); // Thumbs up
            event.getMessage().addReaction(Emoji.fromUnicode("👎")).queue(); // Thumbs down
        } else {
            event.getMessage().delete().queue();
        }
    }

    @Override
    public void onMessageReactionAdd(MessageReactionAddEvent event) {
        if (!event.getChannel().getId().equals(PHOTO_CONTEST_CHANNEL) || event.getUser().isBot()) return;

        Message message = event.retrieveMessage().complete();
        String userId = event.getUserId();
        String emojiAdded = event.getReaction().getEmoji().getName();


        if (!emojiAdded.equals("👍") && !emojiAdded.equals("👎")) return; // Ignore non-thumbs up/down reactions

        List<MessageReaction> reactions = message.getReactions();
        for (MessageReaction reaction : reactions) {
            if (reaction.retrieveUsers().complete().stream().anyMatch(user -> user.getId().equals(userId))) {
                String reactionEmoji = reaction.getEmoji().getName();

                if ((emojiAdded.equals("👍") && reactionEmoji.equals("👎")) ||
                        (emojiAdded.equals("👎") && reactionEmoji.equals("👍"))) {
                    reaction.removeReaction(event.getUser()).queue();
                    return;
                }
            }
        }
    }
}

