package io.github.enoughsdv.discordremote.command;

import com.velocitypowered.api.command.Command;
import com.velocitypowered.api.command.CommandSource;
import io.github.enoughsdv.discordremote.DiscordRemotePlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public class DiscordRemoteCommand implements Command {

    private final DiscordRemotePlugin plugin;

    public DiscordRemoteCommand(DiscordRemotePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSource source, String @NotNull [] args) {
        if (!source.hasPermission("discordremote.reload") && !source.hasPermission("discordremote.op")) {
            source.sendMessage((net.kyori.text.Component) Component.text("You don't have the required permission!", NamedTextColor.RED));
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
                source.sendMessage((net.kyori.text.Component) Component.text("You need to specify a valid subcommand!", NamedTextColor.RED));
                break;
        }
    }

    private void reloadCommand(CommandSource source) {
        source.sendMessage((net.kyori.text.Component) Component.text("Reloading config and bot...").color(NamedTextColor.GREEN));

        try {
            plugin.loadConfig();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        plugin.loadDiscordBot();
    }

    private void helpCommand(CommandSource source) {
        source.sendMessage((net.kyori.text.Component) Component.text("DiscordRemote help").color(NamedTextColor.GREEN));
        source.sendMessage((net.kyori.text.Component) Component.text("/discordremote reload - Reloads the config and the bot.").color(NamedTextColor.GREEN));
    }
}