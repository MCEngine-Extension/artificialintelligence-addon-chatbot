package io.github.mcengine.extension.addon.artificialintelligence.chatbot.listener;

import io.github.mcengine.api.artificialintelligence.util.MCEngineArtificialIntelligenceApiUtilBotManager;
import io.github.mcengine.api.core.extension.addon.MCEngineAddOnLogger;
import io.github.mcengine.common.artificialintelligence.MCEngineArtificialIntelligenceCommon;
import io.github.mcengine.extension.addon.artificialintelligence.chatbot.api.FunctionCallingLoader;
import io.github.mcengine.extension.addon.artificialintelligence.chatbot.command.ChatBotCommand;
import io.github.mcengine.extension.addon.artificialintelligence.chatbot.util.ChatBotConfigLoader;
import io.github.mcengine.extension.addon.artificialintelligence.chatbot.util.ChatBotListenerUtil;
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
import java.util.UUID;

/**
 * Listener that intercepts player chat to handle AI chatbot sessions.
 * Cancels normal chat behavior and forwards messages to the AI backend.
 */
public class ChatBotListener implements Listener {

    /**
     * The plugin instance associated with this listener.
     */
    private final Plugin plugin;

    /**
     * Folder path for configuration and assets.
     */
    private final String folderPath;

    /**
     * Loader for detecting function calls from player messages.
     */
    private final FunctionCallingLoader functionCallingLoader;

    /**
     * The token type to use when interacting with the AI (e.g., "server", "player").
     */
    private final String tokenType;

    /**
     * Constructs a new ChatBotListener.
     *
     * @param plugin The plugin instance.
     * @param folderPath The folder path used for config and resource loading.
     * @param logger Addon logger used during function loading.
     */
    public ChatBotListener(Plugin plugin, String folderPath, MCEngineAddOnLogger logger) {
        this.plugin = plugin;
        this.folderPath = folderPath;
        this.functionCallingLoader = new FunctionCallingLoader(plugin, folderPath, logger);

        File configFile = new File(plugin.getDataFolder(), folderPath + "/config.yml");
        FileConfiguration config = YamlConfiguration.loadConfiguration(configFile);

        this.tokenType = config.getString("token.type", "server");
    }

    /**
     * Handles player chat messages and routes them to the AI if in a session.
     *
     * @param event The async player chat event.
     */
    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();

        if (!MCEngineArtificialIntelligenceApiUtilBotManager.isActive(player)) return;

        event.setCancelled(true);
        event.getRecipients().clear();

        MCEngineArtificialIntelligenceCommon api = MCEngineArtificialIntelligenceCommon.getApi();
        String originalMessage = event.getMessage().trim();

        if (api.checkWaitingPlayer(player)) {
            player.sendMessage(ChatColor.RED + "⏳ Please wait for the AI to respond before sending another message.");
            return;
        }

        // Handle 'quit' command
        if (originalMessage.equalsIgnoreCase("quit")) {
            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                String history = MCEngineArtificialIntelligenceApiUtilBotManager.get(player);
                FileConfiguration config = ChatBotConfigLoader.getCustomConfig(plugin, folderPath);

                boolean mailEnabled = config.getBoolean("mail.enable", false);

                if (mailEnabled) {
                    UUID playerId = player.getUniqueId();
                    String playerEmail = ChatBotCommand.db.getPlayerEmail(playerId);

                    if (playerEmail != null && !playerEmail.isEmpty()) {
                        ChatBotListenerUtil.sendDataToEmail(plugin, folderPath, history, playerEmail);

                        Bukkit.getScheduler().runTask(plugin, () ->
                            player.sendMessage(ChatColor.RED + "Your chat history has been sent to your email!")
                        );
                    } else {
                        plugin.getLogger().warning("mail.enable is true, but no email is registered for player: " + player.getName());
                    }
                }

                MCEngineArtificialIntelligenceApiUtilBotManager.terminate(player);

                Bukkit.getScheduler().runTask(plugin, () ->
                    player.sendMessage(ChatColor.RED + "❌ AI conversation ended.")
                );
            });
            return;
        }

        // Handle normal message
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
