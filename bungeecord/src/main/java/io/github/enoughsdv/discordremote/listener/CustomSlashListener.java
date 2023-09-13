package io.github.enoughsdv.discordremote.listener;

import io.github.enoughsdv.discordremote.DiscordRemotePlugin;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.config.Configuration;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

public class CustomSlashListener extends ListenerAdapter {

    private final DiscordRemotePlugin plugin;
    private final Configuration config;

    public CustomSlashListener(DiscordRemotePlugin plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfig();
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        config.getSection("custom_commands.list").getKeys().stream()
            .filter(string -> event.getName().equalsIgnoreCase(string))
            .filter(string -> {
                List<String> allowedUsers = config.getStringList("custom_commands.list." + string + ".allowed.users");

                if (allowedUsers.isEmpty() || allowedUsers.contains(event.getUser().getId())) {
                    return true;
                } else {
                    event.reply(config.getString("messages.no_permissions")).setEphemeral(true).queue();
                    return false;
                }
            })
            .filter(string -> {
                List<String> allowedRoles = config.getStringList("custom_commands.list." + string + ".allowed.roles");

                if (allowedRoles.isEmpty()) {
                    return true;
                }

                for (String roleId : allowedRoles) {
                    Role role = Objects.requireNonNull(event.getGuild()).getRoleById(roleId);
                    if (Objects.requireNonNull(event.getMember()).getRoles().contains(role)) {
                        return true;
                    }
                }

                event.reply(config.getString("messages.no_permissions")).setEphemeral(true).queue();
                return false;
            })
            .forEach(string -> {
                for (String command : config.getStringList("custom_commands.list." + string + ".actions")) {

                    for (OptionMapping argument : event.getOptions()) {
                        command = command.replace("{" + argument.getName() + "}", argument.getAsString());
                    }

                    plugin.getLogger().info(config.getString("messages.command_logger")
                        .replace("{command}", command)
                        .replace("{user}", event.getInteraction().getUser().getName()));

                    plugin.getProxy().getPluginManager().dispatchCommand(ProxyServer.getInstance().getConsole(), command);

                    event.reply(config.getString("custom_commands.list." + string + ".response")).queue();
                }
            });
    }
}