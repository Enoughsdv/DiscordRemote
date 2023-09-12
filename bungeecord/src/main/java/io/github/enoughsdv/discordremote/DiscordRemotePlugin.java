package io.github.enoughsdv.discordremote;

import io.github.enoughsdv.discordremote.command.DiscordRemoteCommand;
import io.github.enoughsdv.discordremote.listener.CustomSlashListener;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Collections;

public class DiscordRemotePlugin extends Plugin {

    private Configuration config;

    @Override
    public void onLoad() {
        loadConfig();
    }

    @Override
    public void onEnable() {
        loadDiscordBot();

        this.getProxy().getPluginManager().registerCommand(this, new DiscordRemoteCommand(this));
    }

    public void loadDiscordBot() {
        String token = config.getString("settings.discord.token");

        if (token == null || token.isEmpty()) {
            this.getLogger().severe("A token was not found in the configuration, consider using '/discordremote reload' when you have set the token correctly.");
            return;
        }

        JDA jda = JDABuilder.createLight(token, Collections.emptyList())
            .addEventListeners(new CustomSlashListener(this))
            .setActivity(getActivity())
            .build();

        CommandListUpdateAction commandListUpdateAction = jda.updateCommands();

        this.getConfig().getSection("custom_commands.list").getKeys().forEach(string -> {
            SlashCommandData slashCommand = Commands.slash(string,
                    this.getConfig().getString("custom_commands.list." + string + ".description"))
                .setGuildOnly(true);

            this.getConfig().getSection("custom_commands.list." + string + ".arguments").getKeys()
                .forEach(argument -> {
                    OptionData optionData = new OptionData(OptionType
                        .valueOf(this.getConfig().getString("custom_commands.list." + string + ".arguments." + argument + ".type").toUpperCase()),
                        this.getConfig().getString("custom_commands.list." + string + ".arguments." + argument + ".name"),
                        this.getConfig().getString("custom_commands.list." + string + ".arguments." + argument + ".description"),
                        this.getConfig().getBoolean("custom_commands.list." + string + ".arguments." + argument + ".required"));

                    this.getConfig().getSection("custom_commands.list." + string + ".arguments." + argument + ".choices")
                        .getKeys()
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

    public void loadConfig() {
        try {
            if (!getDataFolder().exists()) {
                getDataFolder().mkdir();
            }

            File configFile = new File(getDataFolder(), "config.yml");
            if (!configFile.exists()) {
                InputStream inputStream = getResourceAsStream("config.yml");
                Files.copy(inputStream, configFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }

            config = ConfigurationProvider.getProvider(YamlConfiguration.class).load(configFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Activity getActivity() {
        String activityType = config.getString("settings.discord.activity.type");
        String activityMessage = config.getString("settings.discord.activity.message");
        return Activity.of(Activity.ActivityType.valueOf(activityType), activityMessage);
    }

    public Configuration getConfig() {
        return config;
    }

}
