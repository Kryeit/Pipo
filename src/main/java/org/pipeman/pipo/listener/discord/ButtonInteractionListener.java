package org.pipeman.pipo.listener.discord;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.EnumSet;

public class ButtonInteractionListener extends ListenerAdapter {

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        if (!event.getComponentId().equals("ticket")) {
            return;
        }

        event.deferReply(true).queue(); // Acknowledge the button click as handling might take some time

        final Guild guild = event.getGuild();
        if (guild == null) return; // Check if the event is in a guild

        final Member member = event.getMember();
        if (member == null) return; // Check if we have a member

        Category category = guild.getCategoriesByName("tickets", true).stream().findFirst().orElse(null);
        if (category == null) {
            // Create the category if it doesn't exist
            guild.createCategory("tickets").queue(createdCategory -> {
                createTicketChannel(guild, createdCategory, member, event);
            });
        } else {
            createTicketChannel(guild, category, member, event);
        }
    }

    private void createTicketChannel(Guild guild, Category category, Member member, ButtonInteractionEvent event) {
        // Generate a unique name for the ticket channel
        int random4Digits = (int) (Math.random() * 9000) + 1000;
        // Check if it already exists
        if (category.getTextChannels().stream().anyMatch(channel -> channel.getName().equals("ticket-" + random4Digits))) {
            createTicketChannel(guild, category, member, event);
            return;
        }
        final String channelName = "ticket-" + random4Digits;

        // Create the channel with permissions
        category.createTextChannel(channelName).addPermissionOverride(member, EnumSet.of(Permission.VIEW_CHANNEL), null)
                .addPermissionOverride(guild.getPublicRole(), null, EnumSet.of(Permission.VIEW_CHANNEL))
                .queue(channel -> {
                    channel.sendMessage("Ticket created").queue();

                    channel.createInvite().queue(invite -> {
                        try {
                            event.getHook().sendMessage("Ticket created. You can access it here: " + invite.getUrl()).setEphemeral(true).queue();
                        } catch (Exception e) {
                            System.out.println("Error sending message: " + e.getMessage());
                        }
                    }, throwable -> {
                        System.out.println("Error creating invite: " + throwable.getMessage());
                    });
                });
    }
}
