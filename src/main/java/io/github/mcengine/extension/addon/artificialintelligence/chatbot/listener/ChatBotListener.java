package io.github.mcengine.extension.addon.artificialintelligence.chatbot.listener;

import io.github.mcengine.common.artificialintelligence.MCEngineArtificialIntelligenceCommon;
import io.github.mcengine.api.artificialintelligence.util.MCEngineArtificialIntelligenceApiUtilBotManager;
import io.github.mcengine.extension.addon.artificialintelligence.chatbot.api.FunctionCallingLoader;
import io.github.mcengine.extension.addon.artificialintelligence.chatbot.command.ChatBotCommand;
import io.github.mcengine.extension.addon.artificialintelligence.chatbot.util.ChatBotListenerUtil;
import io.github.mcengine.extension.addon.artificialintelligence.chatbot.util.ChatBotConfigLoader;

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
 * Listens for chat messages from players who are currently in a conversation with the AI.
 * Cancels regular chat output and routes the input to the AI bot manager for processing.
 * Handles session termination and optional email delivery of chat history.
 */
public class ChatBotListener implements Listener {

    private final Plugin plugin;
    private final FunctionCallingLoader functionCallingLoader;
    private final String tokenType;

    /**
     * Constructs the ChatBotListener and loads configuration for token type.
     *
     * @param plugin The plugin instance.
     */
    public ChatBotListener(Plugin plugin) {
        this.plugin = plugin;
        this.functionCallingLoader = new FunctionCallingLoader(plugin);

        File configFile = new File(plugin.getDataFolder(), "configs/addons/MCEngineChatBot/config.yml");
        FileConfiguration config = YamlConfiguration.loadConfiguration(configFile);

        this.tokenType = config.getString("token.type", "server");
    }

    /**
     * Intercepts player chat messages for active AI sessions.
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

        // Handle quit command
        if (originalMessage.equalsIgnoreCase("quit")) {
            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                String history = MCEngineArtificialIntelligenceApiUtilBotManager.get(player);
                FileConfiguration config = ChatBotConfigLoader.getCustomConfig(plugin);

                boolean mailEnabled = config.getBoolean("mail.enable", false);

                if (mailEnabled) {
                    UUID playerId = player.getUniqueId();
                    String playerEmail = ChatBotCommand.db.getPlayerEmail(playerId);

                    if (playerEmail != null && !playerEmail.isEmpty()) {
                        ChatBotListenerUtil.sendDataToEmail(plugin, history, playerEmail);

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

        // Normal message handling
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
