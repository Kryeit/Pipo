package org.pipeman.pipo.commands.minecraft;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import org.pipeman.pipo.Leaderboard;
import org.pipeman.pipo.Leaderboard.LeaderboardEntry;
import org.pipeman.pipo.Leaderboard.Rank;
import org.pipeman.pipo.PotatoManager;

import java.text.MessageFormat;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class CommandPotatoLeaderboard {
    public static int execute(CommandContext<ServerCommandSource> ctx) {
        ServerCommandSource source = ctx.getSource();
        UUID uuid = source.getPlayer().getUuid();
        Optional<Rank> rank = PotatoManager.getRank(uuid);
        List<LeaderboardEntry> leaderboard = PotatoManager.getLeaderboard(Leaderboard.Order.DESC, 0, 10);

        for (int i = 0; i < leaderboard.size(); i++) {
            LeaderboardEntry entry = leaderboard.get(i);
            int rankNumber = i + 1;
            source.sendFeedback(() -> Text.of(MessageFormat.format("{0}: {1} ({2})", rankNumber, entry.name(), entry.value().formatted())), false);
        }
        rank.ifPresent(value -> source.sendFeedback(() -> Text.of(MessageFormat.format("Your standing: {0}: ({1})", value.rank(), value.value().formatted())), false));
        return Command.SINGLE_SUCCESS;
    }

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("potatoleaderboard")
                .requires(ServerCommandSource::isExecutedByPlayer)
                .executes(CommandPotatoLeaderboard::execute));
    }
}
