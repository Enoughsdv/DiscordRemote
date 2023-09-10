package io.github.enoughsdv.discordremote.listeners;

import io.github.enoughsdv.discordremote.DiscordRemotePlugin;
import me.clip.placeholderapi.PlaceholderAPI;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.awt.Color;

public class PlayerChatListener implements Listener {

    private final DiscordRemotePlugin plugin;
    private final FileConfiguration config;

    public PlayerChatListener(DiscordRemotePlugin plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfig();
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        if (!config.getBoolean("chat.enabled")) return;

        for (String channelId : config.getStringList("chat.channels")) {
            TextChannel textChannel = plugin.getJda().getTextChannelById(channelId);

            if (textChannel != null) {

                String url = "https://crafatar.com/avatars/";
                url += event.getPlayer().getUniqueId();
                url += "?&default=MHF_Steve&overlay";

                EmbedBuilder embedBuilder = new EmbedBuilder()
                    .setAuthor(config.getString("chat.embed.title", "Chat Logger"),null,
                        config.getBoolean("chat.embed.show_head_icon") ? url : null)
                    .setDescription(checkPAPI(event.getPlayer(), config.getString("chat.embed.content", "<{player}> {message}"))
                        .replace("{player}", event.getPlayer().getName())
                        .replace("{message}", event.getMessage()))
                    .setColor(Color.decode(config.getString("chat.embed.color", "#FF3232")));

                textChannel.sendMessageEmbeds(embedBuilder.build()).queue();
            }
        }
    }

    private String checkPAPI(Player player, String string) {
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            return PlaceholderAPI.setPlaceholders(player, string);
        }
        return string;
    }

}