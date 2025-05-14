package io.github.mcengine.addon.artificialintelligence.chatbot.listener;

import io.github.mcengine.addon.artificialintelligence.chatbot.util.ChatBotTask;
import io.github.mcengine.addon.artificialintelligence.chatbot.util.ChatBotManager;
import io.github.mcengine.addon.artificialintelligence.chatbot.api.functions.calling.FunctionCallingLoader;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.Bukkit;

import java.util.List;

/**
 * Listens to chat messages from players currently in an AI conversation.
 * Cancels public chat and forwards input to the chatbot task.
 */
public class ChatBotListener implements Listener {

    private final Plugin plugin;
    private final FunctionCallingLoader functionCallingLoader;

    /**
     * Constructor for ChatBotListener.
     *
     * @param plugin The main plugin instance.
     */
    public ChatBotListener(Plugin plugin) {
        this.plugin = plugin;
        this.functionCallingLoader = new FunctionCallingLoader();
    }

    /**
     * Intercepts player chat to handle AI conversation logic.
     *
     * @param event The AsyncPlayerChatEvent.
     */
    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();

        if (!ChatBotManager.isActive(player)) return;

        event.setCancelled(true);
        event.getRecipients().clear();

        if (ChatBotManager.isWaiting(player)) {
            player.sendMessage(ChatColor.RED + "⏳ Please wait for the AI to respond before sending another message.");
            return;
        }

        String originalMessage = event.getMessage().trim();

        if (originalMessage.equalsIgnoreCase("quit")) {
            ChatBotManager.terminate(player);
            Bukkit.getScheduler().runTask(plugin, () ->
                    player.sendMessage(ChatColor.RED + "❌ AI conversation ended.")
            );
            return;
        }

        player.sendMessage(ChatColor.GRAY + "[You → AI]: " + ChatColor.WHITE + originalMessage);

        List<String> matchedResponses = functionCallingLoader.match(player, originalMessage);
        String finalMessage = originalMessage;

        if (!matchedResponses.isEmpty()) {
            // Append all matched function responses into the prompt
            StringBuilder sb = new StringBuilder(originalMessage).append("\n\n[Function Info]\n");
            for (String response : matchedResponses) {
                sb.append("- ").append(response).append("\n");
            }
            finalMessage = sb.toString();
        }

        ChatBotManager.setWaiting(player, true);
        String platform = ChatBotManager.getPlatform(player);
        String model = ChatBotManager.getModel(player);

        new ChatBotTask(plugin, player, platform, model, finalMessage).runTaskAsynchronously(plugin);
    }
}
