package io.github.mcengine.addon.artificialintelligence.chatbot.listener;

import io.github.mcengine.addon.artificialintelligence.chatbot.util.ChatBotTask;
import io.github.mcengine.addon.artificialintelligence.chatbot.util.ChatBotManager;
import io.github.mcengine.api.artificialintelligence.MCEngineArtificialIntelligenceApi;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

public class ChatBotListener implements Listener {

    private final Plugin plugin;

    public ChatBotListener(Plugin plugin) {
        this.plugin = plugin;
    }

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

        String message = event.getMessage().trim();

        if (message.equalsIgnoreCase("quit")) {
            ChatBotManager.terminate(player);
            new BukkitRunnable() {
                @Override
                public void run() {
                    player.sendMessage(ChatColor.RED + "❌ AI conversation ended.");
                }
            }.runTask(plugin);
            return;
        }

        player.sendMessage(ChatColor.GRAY + "[You → AI]: " + ChatColor.WHITE + message);
        ChatBotManager.append(player, "[Player]: " + message);
        ChatBotManager.setWaiting(player, true);

        String platform = ChatBotManager.getPlatform(player);
        String model = ChatBotManager.getModel(player);

        new ChatBotTask(plugin, player, platform, model, message).runTaskAsynchronously(plugin);
    }
}
