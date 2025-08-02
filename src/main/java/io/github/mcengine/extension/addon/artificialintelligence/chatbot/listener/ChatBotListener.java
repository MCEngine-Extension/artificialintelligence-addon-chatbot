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
     * The token type to use when interacting with the AI (e.g., "server", "player").
     */
    private final String tokenType;

    /**
     * System prompt prepended to AI context.
     */
    private final String systemPrompt;

    /**
     * Constructs a new ChatBotListener.
     *
     * @param plugin     The plugin instance.
     * @param folderPath The folder path used for config and resource loading.
     * @param logger     Addon logger used during function loading.
     */
    public ChatBotListener(Plugin plugin, String folderPath, MCEngineExtensionLogger logger) {
        this.plugin = plugin;
        this.folderPath = folderPath;

        File configFile = new File(plugin.getDataFolder(), folderPath + "/config.yml");
        FileConfiguration config = YamlConfiguration.loadConfiguration(configFile);

        this.tokenType = config.getString("token.type", "server");
        this.systemPrompt = config.getString("ai.system.prompt", "");
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
                plugin.getLogger().warning("AI chat failed for " + player.getName() + ": " + e.getMessage());
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
