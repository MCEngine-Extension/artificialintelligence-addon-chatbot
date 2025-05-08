package io.github.mcengine.addon.artificialintelligence.chatbot.command;

import io.github.mcengine.api.artificialintelligence.MCEngineArtificialIntelligenceApi;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Async task to call the AI API and respond to the player.
 */
public class ChatBotTask extends BukkitRunnable {

    private final Plugin plugin;
    private final Player player;
    private final String platform;
    private final String model;
    private final String message;

    public ChatBotTask(Plugin plugin, Player player, String platform, String model, String message) {
        this.plugin = plugin;
        this.player = player;
        this.platform = platform;
        this.model = model;
        this.message = message;
    }

    @Override
    public void run() {
        MCEngineArtificialIntelligenceApi api = MCEngineArtificialIntelligenceApi.getApi();
        String response;
        try {
            response = api.getResponse(platform, model, message);
        } catch (Exception e) {
            Bukkit.getScheduler().runTask(plugin, () ->
                player.sendMessage("§c[ChatBot] Failed: " + e.getMessage())
            );
            return;
        }

        Bukkit.getScheduler().runTask(plugin, () ->
            player.sendMessage("§e[ChatBot]§r " + response)
        );
    }
}
