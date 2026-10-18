package me.justahuman.slimefun_server_essentials.command;

import me.justahuman.slimefun_server_essentials.SlimefunServerEssentials;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class MonaJeiCommand implements CommandExecutor, TabCompleter {
    private static final List<String> SUBCOMMANDS = List.of("status", "reload", "sync", "syncall", "export", "debug");
    private final SlimefunServerEssentials plugin;

    public MonaJeiCommand(SlimefunServerEssentials plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {
        String subcommand = args.length == 0 ? "status" : args[0].toLowerCase(Locale.ROOT);
        if (!SUBCOMMANDS.contains(subcommand)) {
            sender.sendMessage("§cUsage: /" + label + " <status|reload|sync|syncall|export|debug>");
            return true;
        }
        if (!sender.hasPermission("monajei." + subcommand)) {
            sender.sendMessage("§c権限がありません: monajei." + subcommand);
            return true;
        }

        switch (subcommand) {
            case "status" -> plugin.sendStatus(sender);
            case "reload" -> {
                plugin.reloadConfig();
                plugin.rebuildData(false);
                sender.sendMessage("§aMona JEI の設定と同期キャッシュを再構築しました。");
            }
            case "sync" -> {
                if (args.length < 2) {
                    sender.sendMessage("§cUsage: /" + label + " sync <player>");
                    return true;
                }
                Player target = Bukkit.getPlayerExact(args[1]);
                if (target == null) {
                    sender.sendMessage("§cオンラインのプレイヤーが見つかりません。");
                    return true;
                }
                SlimefunServerEssentials.getLoadingStateChannel().sync(target);
                sender.sendMessage("§a" + target.getName() + " へ再同期を開始しました。");
            }
            case "syncall" -> {
                Bukkit.getOnlinePlayers().forEach(SlimefunServerEssentials.getLoadingStateChannel()::sync);
                sender.sendMessage("§aオンラインプレイヤーへの再同期を開始しました。");
            }
            case "export" -> {
                plugin.rebuildData(true);
                sender.sendMessage("§aJSONを plugins/MonaSlimefunServerEssentials/generated に出力しました。");
            }
            case "debug" -> {
                boolean enabled = !plugin.getConfig().getBoolean("debug", false);
                plugin.getConfig().set("debug", enabled);
                plugin.saveConfig();
                sender.sendMessage("§aデバッグログ: " + (enabled ? "ON" : "OFF"));
            }
            default -> throw new IllegalStateException("Unexpected command: " + subcommand);
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                      @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) {
            String prefix = args[0].toLowerCase(Locale.ROOT);
            return SUBCOMMANDS.stream().filter(value -> value.startsWith(prefix)).toList();
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("sync")) {
            String prefix = args[1].toLowerCase(Locale.ROOT);
            List<String> players = new ArrayList<>();
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (player.getName().toLowerCase(Locale.ROOT).startsWith(prefix)) {
                    players.add(player.getName());
                }
            }
            return players;
        }
        return List.of();
    }
}
