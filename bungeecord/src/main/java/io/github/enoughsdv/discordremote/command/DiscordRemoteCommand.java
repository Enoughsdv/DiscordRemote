package io.github.enoughsdv.discordremote.command;

import io.github.enoughsdv.discordremote.DiscordRemotePlugin;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Command;

public class DiscordRemoteCommand extends Command {

    private final DiscordRemotePlugin plugin;

    public DiscordRemoteCommand(DiscordRemotePlugin plugin) {
        super("discordremote");
        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!sender.hasPermission("discordremote.reload")) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cYou don't have the required permission!"));
            return;
        }

        if (args.length == 0) {
            helpCommand(sender);
            return;
        }

        switch (args[0]) {
            case "reload":
                reloadCommand(sender);
                break;
            case "help":
                helpCommand(sender);
                break;
            default:
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cYou need to specify a valid subcommand!"));
                break;
        }
    }

    private void reloadCommand(CommandSender sender) {
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&aReloading config and bot..."));

        plugin.loadConfig();
        plugin.loadDiscordBot();
    }

    private void helpCommand(CommandSender sender) {
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&aDiscordRemote help"));
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&a/discordremote reload - Reloads the config and the bot."));
    }

}
