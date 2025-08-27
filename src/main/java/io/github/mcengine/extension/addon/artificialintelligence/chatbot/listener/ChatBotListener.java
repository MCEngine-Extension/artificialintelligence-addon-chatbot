package io.github.mcengine.extension.addon.artificialintelligence.chatbot.listener;

import com.google.gson.JsonObject;
import io.github.mcengine.api.artificialintelligence.util.MCEngineArtificialIntelligenceApiUtilBotManager;
import io.github.mcengine.api.core.extension.logger.MCEngineExtensionLogger;
import io.github.mcengine.common.artificialintelligence.MCEngineArtificialIntelligenceCommon;
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
 * <p>
 * Responsibilities:
 * <ul>
 *   <li>Gate normal chat when a player is in an AI session.</li>
 *   <li>Forward messages to the AI backend using the configured token type.</li>
 *   <li>Handle special commands (e.g., {@code quit}) and optional email export.</li>
 *   <li>Log operational details via {@link MCEngineExtensionLogger} with contextual prefixes.</li>
 * </ul>
 */
public class ChatBotListener implements Listener {

    /**
     * The plugin instance associated with this listener.
     * Used for scheduler dispatch and data-folder resolution.
     */
    private final Plugin plugin;

    /**
     * Folder path for configuration and assets (relative to the plugin data folder).
     */
    private final String folderPath;

    /**
     * The token type to use when interacting with the AI (e.g., "server" or "player").
     */
    private final String tokenType;

    /**
     * System prompt prepended to AI context (optional; may be empty).
     */
    private final String systemPrompt;

    /**
     * Extension-aware logger that prefixes messages with plugin / context info.
     */
    private final MCEngineExtensionLogger logger;

    /**
     * Constructs a new ChatBotListener.
     *
     * @param plugin     The plugin instance.
     * @param folderPath The folder path used for config and resource loading (relative to plugin data folder).
     * @param logger     Extension logger used for contextual logging.
     */
    public ChatBotListener(Plugin plugin, String folderPath, MCEngineExtensionLogger logger) {
        this.plugin = plugin;
        this.folderPath = folderPath;
        this.logger = logger;

        File configFile = new File(plugin.getDataFolder(), folderPath + "/config.yml");
        FileConfiguration config = YamlConfiguration.loadConfiguration(configFile);

        this.tokenType = config.getString("token.type", "server");
        this.systemPrompt = config.getString("ai.system.prompt", "");
    }

    /**
     * Handles player chat messages and routes them to the AI if the player is currently in a session.
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
                        logger.warning("mail.enable is true, but no email is registered for player: " + player.getName());
                    }
                }

                MCEngineArtificialIntelligenceApiUtilBotManager.terminate(player);

                Bukkit.getScheduler().runTask(plugin, () ->
                    player.sendMessage(ChatColor.RED + "❌ AI conversation ended.")
                );
            });
            return;
        }

        // Handle normal AI message
        player.sendMessage(ChatColor.GRAY + "[You → AI]: " + ChatColor.WHITE + originalMessage);

        String match = api.getMessageMatch(player, originalMessage);
        final String preparedMessage;
        if (match != null) {
            preparedMessage = originalMessage + "\n\n[Function Info]\n- " + match;
        } else {
            preparedMessage = originalMessage;
        }

        final String platform = MCEngineArtificialIntelligenceApiUtilBotManager.getPlatform(player);
        final String model = MCEngineArtificialIntelligenceApiUtilBotManager.getModel(player);

        // Mark the player as waiting to prevent duplicate task execution
        api.setWaiting(player, true);

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                JsonObject response;

                if ("server".equalsIgnoreCase(tokenType)) {
                    String context = MCEngineArtificialIntelligenceApiUtilBotManager.get(player);
                    response = api.getResponse(platform, model, context, preparedMessage);
                } else if ("player".equalsIgnoreCase(tokenType)) {
                    String token = api.getPlayerToken(player.getUniqueId().toString(), platform);
                    if (token == null || token.isEmpty()) {
                        throw new IllegalStateException("No token found for player.");
                    }
                    String context = MCEngineArtificialIntelligenceApiUtilBotManager.get(player);
                    response = api.getResponse(platform, model, token, context, preparedMessage);
                } else {
                    throw new IllegalArgumentException("Unknown tokenType: " + tokenType);
                }

                String reply = api.getCompletionContent(response);
                int tokensUsed = api.getTotalTokenUsage(response);

                // Update conversation
                MCEngineArtificialIntelligenceApiUtilBotManager.append(player, "[Player]: " + originalMessage);
                MCEngineArtificialIntelligenceApiUtilBotManager.append(player, "[AI]: " + reply);

                // Send AI reply
                Bukkit.getScheduler().runTask(plugin, () -> {
                    player.sendMessage(ChatColor.GOLD + "[AI → You]: " + ChatColor.RESET + reply);
                    if (tokensUsed >= 0) {
                        player.sendMessage(ChatColor.GREEN + "[Tokens Used] " + ChatColor.WHITE + tokensUsed);
                    }
                });

            } catch (Exception e) {
                logger.warning("AI chat failed for " + player.getName() + ": " + e.getMessage());
                Bukkit.getScheduler().runTask(plugin, () ->
                    player.sendMessage(ChatColor.RED + "❌ Failed to process your AI message.")
                );
            } finally {
                // Unmark the player as waiting regardless of success or failure
                api.setWaiting(player, false);
            }
        });
    }
}
