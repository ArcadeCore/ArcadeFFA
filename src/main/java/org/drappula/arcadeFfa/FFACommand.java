package org.drappula.arcadeFfa;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import com.mojang.brigadier.tree.LiteralCommandNode;
import org.bukkit.entity.Player;
import org.drappula.arcadeApi.ArcadeAPIProvider;
import org.drappula.arcadeApi.systems.queue.JoinResult;

public class FFACommand {
    public static LiteralCommandNode<CommandSourceStack> get(FFAGame game) {
        return Commands.literal("ffa")
                .executes(FFACommand::info)
                .then(Commands.literal("join").executes(ctx -> join(ctx, game)))
                .then(Commands.literal("leave").executes(FFACommand::leave))
                .build();
    }

    private static int info(CommandContext<CommandSourceStack> ctx) {
        ctx.getSource().getSender().sendRichMessage("<aqua><b>Free For All</b></aqua> <gray>- /ffa join | /ffa leave");
        return Command.SINGLE_SUCCESS;
    }

    private static int join(CommandContext<CommandSourceStack> ctx, FFAGame game) {
        if (!(ctx.getSource().getSender() instanceof Player player)) {
            ctx.getSource().getSender().sendRichMessage("<red>Only players can join the FFA queue.");
            return Command.SINGLE_SUCCESS;
        }
        JoinResult result = ArcadeAPIProvider.get().getQueueManager().joinQueue(player, game);
        if (result != JoinResult.SUCCESS) {
            player.sendRichMessage("<red>Failed to join the queue: " + result.name().toLowerCase().replace('_', ' '));
            return Command.SINGLE_SUCCESS;
        }
        player.sendRichMessage("<green>Joined the FFA queue.");
        return Command.SINGLE_SUCCESS;
    }

    private static int leave(CommandContext<CommandSourceStack> ctx) {
        if (!(ctx.getSource().getSender() instanceof Player player)) {
            ctx.getSource().getSender().sendRichMessage("<red>Only players can leave the FFA queue.");
            return Command.SINGLE_SUCCESS;
        }
        ArcadeAPIProvider.get().getQueueManager().leaveQueue(player);
        player.sendRichMessage("<yellow>Left the FFA queue.");
        return Command.SINGLE_SUCCESS;
    }
}
