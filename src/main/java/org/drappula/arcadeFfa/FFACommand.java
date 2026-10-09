package org.drappula.arcadeFfa;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.drappula.arcadeApi.ArcadeAPIProvider;
import org.drappula.arcadeApi.message.Messages;
import org.drappula.arcadeApi.systems.queue.JoinResult;

/** {@code /ffa}. A plain Bukkit executor so it works on every server version. */
public class FFACommand implements CommandExecutor, TabCompleter {
    private static final List<String> SUBCOMMANDS = Arrays.asList("join", "leave");

    private final FFAGame game;

    public FFACommand(FFAGame game) {
        this.game = game;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String sub = args.length == 0 ? "" : args[0].toLowerCase(Locale.ROOT);
        if (sub.equals("join")) {
            join(sender);
        } else if (sub.equals("leave")) {
            leave(sender);
        } else {
            Messages.chat(sender, "<aqua><b>Free For All</b></aqua> <gray>- /ffa join | /ffa leave");
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> out = new ArrayList<String>();
        if (args.length != 1) return out;
        for (String sub : SUBCOMMANDS) {
            if (sub.startsWith(args[0].toLowerCase(Locale.ROOT))) out.add(sub);
        }
        return out;
    }

    private void join(CommandSender sender) {
        if (!(sender instanceof Player)) {
            Messages.chat(sender, "<red>Only players can join the FFA queue.");
            return;
        }
        Player player = (Player) sender;
        JoinResult result = ArcadeAPIProvider.get().getQueueManager().joinQueue(player, game);
        if (result != JoinResult.SUCCESS) {
            Messages.chat(player, "<red>Failed to join the queue: <reason>",
                    "reason", result.name().toLowerCase(Locale.ROOT).replace('_', ' '));
            return;
        }
        Messages.chat(player, "<green>Joined the FFA queue.");
    }

    private void leave(CommandSender sender) {
        if (!(sender instanceof Player)) {
            Messages.chat(sender, "<red>Only players can leave the FFA queue.");
            return;
        }
        Player player = (Player) sender;
        ArcadeAPIProvider.get().getQueueManager().leaveQueue(player);
        Messages.chat(player, "<yellow>Left the FFA queue.");
    }
}
