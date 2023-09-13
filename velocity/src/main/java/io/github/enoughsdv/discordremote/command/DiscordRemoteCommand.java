package io.github.enoughsdv.discordremote.command;

import com.velocitypowered.api.command.CommandSource;
import io.github.enoughsdv.discordremote.DiscordRemotePlugin;
import com.velocitypowered.api.command.SimpleCommand;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public class DiscordRemoteCommand implements SimpleCommand {

    private final DiscordRemotePlugin plugin;

    public DiscordRemoteCommand(DiscordRemotePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(final Invocation invocation) {
        CommandSource source = invocation.source();
        String[] args = invocation.arguments();
        if (!source.hasPermission("discordremote.reload") && !source.hasPermission("discordremote.op")) {
            source.sendMessage(Component.text("You don't have the required permission!", NamedTextColor.RED));
            return;
        }

        if (args.length == 0) {
            helpCommand(source);
            return;
        }

        switch (args[0]) {
            case "reload":
                reloadCommand(source);
                break;
            case "help":
                helpCommand(source);
                break;
            default:
                source.sendMessage(Component.text("You need to specify a valid subcommand!", NamedTextColor.RED));
                break;
        }
    }

    private void reloadCommand(CommandSource source) {
        source.sendMessage(Component.text("Reloading config and bot...").color(NamedTextColor.GREEN));

        plugin.loadConfig();
        plugin.loadDiscordBot();
    }

    private void helpCommand(CommandSource source) {
        source.sendMessage(Component.text("DiscordRemote help").color(NamedTextColor.GREEN));
        source.sendMessage(Component.text("/discordremote reload - Reloads the config and the bot.").color(NamedTextColor.GREEN));
    }
}