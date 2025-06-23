package io.github.mcengine.addon.artificialintelligence.chatbot.listener;

import io.github.mcengine.api.artificialintelligence.MCEngineArtificialIntelligenceApi;
import io.github.mcengine.api.artificialintelligence.util.MCEngineArtificialIntelligenceApiUtilBotManager;
import io.github.mcengine.addon.artificialintelligence.chatbot.api.FunctionCallingLoader;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.Plugin;

import java.io.File;

import java.util.List;

/**
 * Listens to chat messages from players currently in an AI conversation.
 * Cancels public chat and forwards input to the chatbot task.
 */
public class ChatBotListener implements Listener {

    private final Plugin plugin;
    private final FunctionCallingLoader functionCallingLoader;
    private final String tokenType;

    /**
     * Constructor for ChatBotListener.
     *
     * @param plugin The main plugin instance.
     */
    public ChatBotListener(Plugin plugin) {
        this.plugin = plugin;
        this.functionCallingLoader = new FunctionCallingLoader(plugin);
        // Load custom config file
        File configFile = new File(plugin.getDataFolder(), "addons/MCEngineChatBot/config.yml");
        FileConfiguration config = YamlConfiguration.loadConfiguration(configFile);

        this.tokenType = config.getString("token.type", "server");
    }

    /**
     * Intercepts player chat to handle AI conversation logic.
     *
     * @param event The AsyncPlayerChatEvent.
     */
    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();

        if (!MCEngineArtificialIntelligenceApiUtilBotManager.isActive(player)) return;

        event.setCancelled(true);
        event.getRecipients().clear();

        MCEngineArtificialIntelligenceApi api = MCEngineArtificialIntelligenceApi.getApi();

        String originalMessage = event.getMessage().trim();

        // Prevent quitting while waiting
        if (api.checkWaitingPlayer(player)) {
            player.sendMessage(ChatColor.RED + "⏳ Please wait for the AI to respond before sending another message.");
            return;
        }

        // Only allow quit after AI has responded
        if (originalMessage.equalsIgnoreCase("quit")) {
            MCEngineArtificialIntelligenceApiUtilBotManager.terminate(player);
            Bukkit.getScheduler().runTask(plugin, () ->
                player.sendMessage(ChatColor.RED + "❌ AI conversation ended.")
            );
            return;
        }

        player.sendMessage(ChatColor.GRAY + "[You → AI]: " + ChatColor.WHITE + originalMessage);

        List<String> matchedResponses = functionCallingLoader.match(player, originalMessage);
        String finalMessage = originalMessage;

        if (!matchedResponses.isEmpty()) {
            StringBuilder sb = new StringBuilder(originalMessage).append("\n\n[Function Info]\n");
            for (String response : matchedResponses) {
                sb.append("- ").append(response).append("\n");
            }
            finalMessage = sb.toString();
        }

        String platform = MCEngineArtificialIntelligenceApiUtilBotManager.getPlatform(player);
        String model = MCEngineArtificialIntelligenceApiUtilBotManager.getModel(player);

        api.runBotTask(player, tokenType, platform, model, finalMessage);
    }
}
