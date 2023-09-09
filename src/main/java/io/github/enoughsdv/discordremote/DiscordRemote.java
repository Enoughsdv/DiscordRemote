package io.github.enoughsdv.discordremote;

import io.github.enoughsdv.discordremote.listener.CustomSlashCommand;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Collections;

public final class DiscordRemote extends JavaPlugin {

    @Override
    public void onEnable() {
        this.saveDefaultConfig();
        loadDiscordBot();
    }

    private void loadDiscordBot() {
        JDA jda = JDABuilder.createLight(this.getConfig().getString("settings.discord.token"), Collections.emptyList())
            .addEventListeners(new CustomSlashCommand(this))
            .setActivity(Activity.of(Activity.ActivityType.valueOf(this.getConfig().getString("settings.discord.activity.type")),
                this.getConfig().getString("settings.discord.activity.message")))
            .build();

        CommandListUpdateAction commandListUpdateAction = jda.updateCommands();

        this.getConfig().getConfigurationSection("custom_commands.list").getKeys(false).forEach(string -> {
            this.getLogger().info("Registered discord slash command: " + string);

            SlashCommandData slashCommand = Commands.slash(string,
                    this.getConfig().getString("custom_commands.list." + string + ".description"))
                .setGuildOnly(true);

            this.getConfig().getConfigurationSection("custom_commands.list." + string + ".arguments").getKeys(false)
                .forEach(argument -> slashCommand.addOption(OptionType.valueOf(this.getConfig().getString("custom_commands.list." + string + ".arguments." + argument + ".type").toUpperCase()),
                    this.getConfig().getString("custom_commands.list." + string + ".arguments." + argument + ".name"),
                    this.getConfig().getString("custom_commands.list." + string + ".arguments." + argument + ".description"),
                    this.getConfig().getBoolean("custom_commands.list." + string + ".arguments." + argument + ".required")));

            commandListUpdateAction.addCommands(slashCommand);
        });
        commandListUpdateAction.queue();
    }
}
