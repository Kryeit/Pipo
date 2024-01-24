package org.pipeman.pipo.commands.minecraft;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.pipeman.pipo.Pipo;

import java.lang.reflect.Member;
import java.util.Objects;
import java.util.function.Supplier;

public class CommandUnlinkDiscord {
    public static int execute(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        ServerPlayerEntity player = source.getPlayer();

        if (player == null) {
            Supplier<Text> message = () -> Text.of("Can't execute from console");
            source.sendFeedback(message, false);
            return 0;
        }

        String member = Pipo.getInstance().minecraftToDiscord.getElement(player.getUuid());

        if (Objects.equals(member, "") || member == null) {
            Supplier<Text> message = () -> Text.of("You haven't linked your account");
            source.sendFeedback(message, false);
            return 0;
        }

        try {
            Pipo.getInstance().minecraftToDiscord.removeElement(player.getUuid());
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }

        player.sendMessage(Text.literal("Your account has been unlinked"));
        return Command.SINGLE_SUCCESS;
    }

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("unlinkdiscord")
                .executes(CommandUnlinkDiscord::execute)
        );
    }
}
