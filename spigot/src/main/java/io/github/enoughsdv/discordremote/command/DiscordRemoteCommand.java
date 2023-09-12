package io.github.enoughsdv.discordremote.command;

import io.github.enoughsdv.discordremote.DiscordRemotePlugin;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class DiscordRemoteCommand implements CommandExecutor {

    private final DiscordRemotePlugin plugin;

    public DiscordRemoteCommand(DiscordRemotePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("discordremote.reload") || !sender.isOp()) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cYou don't have the required permission!"));
            return true;
        }

        if (args.length == 0) {
            return helpCommand(sender);
        }

        switch (args[0]) {
            case "reload":
                return reloadCommand(sender);
            case "help":
                return helpCommand(sender);
            default:
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cYou need to specify a valid subcommand!"));
                return true;
        }
    }

    private boolean reloadCommand(CommandSender sender) {
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&aReloading config and bot..."));

        plugin.reloadConfig();
        plugin.loadDiscordBot();
        return true;
    }

    private boolean helpCommand(CommandSender sender) {
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&aDiscordRemote help"));
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&a/discordremote reload - Reloads the config and the bot."));
        return true;
    }
}
