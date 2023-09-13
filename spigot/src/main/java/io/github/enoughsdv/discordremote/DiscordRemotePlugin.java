package io.github.enoughsdv.discordremote;

import io.github.enoughsdv.discordremote.command.DiscordRemoteCommand;
import io.github.enoughsdv.discordremote.listeners.CustomSlashListener;
import io.github.enoughsdv.discordremote.listeners.PlayerChatListener;
import org.bukkit.plugin.java.JavaPlugin;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;

import java.util.Collections;

public class DiscordRemotePlugin extends JavaPlugin {

    private JDA jda;

    @Override
    public void onEnable() {
        this.saveDefaultConfig();
        loadDiscordBot();

        this.getCommand("discordremote").setExecutor(new DiscordRemoteCommand(this));
        this.getServer().getPluginManager().registerEvents(new PlayerChatListener(this), this);
    }

    public void loadDiscordBot() {
        String token = this.getConfig().getString("settings.discord.token");

        if (token == null || token.isEmpty()) {
            this.getLogger().warning("A token was not found in the configuration, consider using '/discordremote reload' when you have set the token correctly.");
            return;
        }

        jda = JDABuilder.createLight(token, Collections.emptyList())
            .addEventListeners(new CustomSlashListener(this))
            .setActivity(getActivity())
            .build();

        CommandListUpdateAction commandListUpdateAction = jda.updateCommands();

        this.getConfig().getConfigurationSection("custom_commands.list").getKeys(false).forEach(string -> {
            SlashCommandData slashCommand = Commands.slash(string,
                    this.getConfig().getString("custom_commands.list." + string + ".description"))
                .setGuildOnly(true);

            this.getConfig().getConfigurationSection("custom_commands.list." + string + ".arguments").getKeys(false)
                .forEach(argument -> {
                    OptionData optionData = new OptionData(OptionType
                        .valueOf(this.getConfig().getString("custom_commands.list." + string + ".arguments." + argument + ".type").toUpperCase()),
                        this.getConfig().getString("custom_commands.list." + string + ".arguments." + argument + ".name"),
                        this.getConfig().getString("custom_commands.list." + string + ".arguments." + argument + ".description"),
                        this.getConfig().getBoolean("custom_commands.list." + string + ".arguments." + argument + ".required"));

                    this.getConfig().getConfigurationSection("custom_commands.list." + string + ".arguments." + argument + ".choices")
                        .getKeys(false)
                        .forEach(choice -> optionData.addChoice(this.getConfig().getString("custom_commands.list." + string + ".arguments." + argument + ".choices." + choice + ".name"),
                            this.getConfig().getString("custom_commands.list." + string + ".arguments." + argument + ".choices." + choice + ".value")));

                    slashCommand.addOptions(optionData);
                });

            this.getLogger().info("Registering discord slash command: " + string);
            commandListUpdateAction.addCommands(slashCommand);
        });

        commandListUpdateAction.queue(success -> this.getLogger().info("Successfully registered discord slash commands"),
            failure -> this.getLogger().warning("Failed to register discord slash commands"));
    }

    private Activity getActivity() {
        String activityType = this.getConfig().getString("settings.discord.activity.type");
        String activityMessage = this.getConfig().getString("settings.discord.activity.message");
        return Activity.of(Activity.ActivityType.valueOf(activityType), activityMessage);
    }

    public JDA getJda() {
        return jda;
    }

}
