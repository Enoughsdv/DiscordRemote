package io.github.enoughsdv.discordremote;

import com.google.inject.Inject;
import com.velocitypowered.api.command.CommandMeta;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.ProxyServer;
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
import org.simpleyaml.configuration.file.YamlFile;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Collections;
import java.util.logging.Logger;

@Plugin(id = "discordremote", name = "DiscordRemote", version = "1.0", authors = {"Enoughsdv"})
public class DiscordRemotePlugin {

    private final ProxyServer proxyServer;
    private final Logger logger;

    private YamlFile config;

    @Inject
    public DiscordRemotePlugin(ProxyServer proxyServer, Logger logger) {
        this.proxyServer = proxyServer;
        this.logger = logger;
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        loadConfig();
        loadDiscordBot();

        CommandMeta commandMeta = proxyServer.getCommandManager().metaBuilder("discordremote")
            .aliases("dremote", "discordr")
            .plugin(this)
            .build();

        proxyServer.getCommandManager().register(commandMeta, new DiscordRemoteCommand(this));
    }

    public void loadDiscordBot() {

        String token = config.getString("settings.discord.token");

        if (token == null || token.isEmpty()) {
            logger.warning("A token was not found in the configuration, consider using '/discordremote reload' when you have set the token correctly.");
            return;
        }

        JDA jda = JDABuilder.createLight(config.getString("settings.discord.token"), Collections.emptyList())
            .addEventListeners(new CustomSlashListener(this))
            .setActivity(getActivity())
            .build();

        CommandListUpdateAction commandListUpdateAction = jda.updateCommands();

        config.getConfigurationSection("custom_commands.list").getKeys(false).forEach(string -> {
            SlashCommandData slashCommand = Commands.slash(string,
                    config.getString("custom_commands.list." + string + ".description"))
                .setGuildOnly(true);

            config.getConfigurationSection("custom_commands.list." + string + ".arguments").getKeys(false)
                .forEach(argument -> {
                    OptionData optionData = new OptionData(OptionType
                        .valueOf(config.getString("custom_commands.list." + string + ".arguments." + argument + ".type").toUpperCase()),
                        config.getString("custom_commands.list." + string + ".arguments." + argument + ".name"),
                        config.getString("custom_commands.list." + string + ".arguments." + argument + ".description"),
                        config.getBoolean("custom_commands.list." + string + ".arguments." + argument + ".required"));

                    config.getConfigurationSection("custom_commands.list." + string + ".arguments." + argument + ".choices")
                        .getKeys(false)
                        .forEach(choice -> optionData.addChoice(config.getString("custom_commands.list." + string + ".arguments." + argument + ".choices." + choice + ".name"),
                            config.getString("custom_commands.list." + string + ".arguments." + argument + ".choices." + choice + ".value")));

                    slashCommand.addOptions(optionData);
                });

            logger.info("Registering discord slash command: " + string);
            commandListUpdateAction.addCommands(slashCommand);
        });

        commandListUpdateAction.queue(success -> logger.info("Successfully registered discord slash commands"),
            failure -> logger.warning("Failed to register discord slash commands"));
    }

    public void loadConfig() {
        config = new YamlFile(new File("plugins/discordremote/config.yml"));
        try {
            if (!config.exists() || config.isEmpty()) {
                config.createNewFile();
                InputStream inputStream = getClass().getResourceAsStream("/config.yml");
                assert inputStream != null;
                Files.copy(inputStream, config.getConfigurationFile().toPath(), StandardCopyOption.REPLACE_EXISTING);
            }

            config.load();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Activity getActivity() {
        String activityType = config.getString("settings.discord.activity.type");
        String activityMessage = config.getString("settings.discord.activity.message");
        return Activity.of(Activity.ActivityType.valueOf(activityType), activityMessage);
    }

    public YamlFile getConfig() {
        return config;
    }

    public Logger getLogger() {
        return logger;
    }

    public ProxyServer getProxyServer() {
        return proxyServer;
    }

}
