package io.github.enoughsdv.discordremote.listener;

import io.github.enoughsdv.discordremote.DiscordRemote;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

public class CustomSlashCommand extends ListenerAdapter {

    private final DiscordRemote plugin;
    private final FileConfiguration config;

    public CustomSlashCommand(DiscordRemote plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfig();
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {

        config.getConfigurationSection("custom_commands.list").getKeys(false).stream()
            .filter(string -> event.getName().equalsIgnoreCase(string))
            .filter(string -> {
                List<String> allowedUsers = config.getStringList("custom_commands.list." + string + ".allowed.users");

                if (allowedUsers.isEmpty()) {
                    return true;
                } else {
                    event.reply(config.getString("messages.no_permissions")).setEphemeral(true).queue();
                    return allowedUsers.contains(event.getUser().getId());
                }
            })
            .filter(string -> {
                List<String> allowedRoles = config.getStringList("custom_commands.list." + string + ".allowed.roles");

                if (allowedRoles.isEmpty()) {
                    return true;
                } else {
                    for (String roleId : allowedRoles) {
                        Role role = Objects.requireNonNull(event.getGuild()).getRoleById(roleId);
                        if (Objects.requireNonNull(event.getMember()).getRoles().contains(role)) {
                            return true;
                        }
                    }
                    event.reply(config.getString("messages.no_permissions")).setEphemeral(true).queue();
                    return false;
                }
            })
            .forEach(string -> {
                for (String command : config.getStringList("custom_commands.list." + string + ".actions")) {

                    for (OptionMapping argument : event.getOptions()) {
                        command = command.replace("{" + argument.getName() + "}", argument.getAsString());
                    }

                    Bukkit.getLogger().info("Executed custom command: " + command + " | By user: " + event.getInteraction().getUser().getName());

                    if (Bukkit.isPrimaryThread()) {
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
                    } else {
                        String finalCommand = command;
                        Bukkit.getScheduler().runTask(plugin, () -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), finalCommand));
                    }

                    event.reply("You have successfully executed the command").queue();
                }
            });
    }
}