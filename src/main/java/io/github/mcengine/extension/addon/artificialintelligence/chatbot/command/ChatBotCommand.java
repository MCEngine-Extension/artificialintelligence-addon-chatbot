package io.github.mcengine.extension.addon.artificialintelligence.chatbot.command;

import io.github.mcengine.api.artificialintelligence.util.MCEngineArtificialIntelligenceApiUtilAi;
import io.github.mcengine.api.artificialintelligence.util.MCEngineArtificialIntelligenceApiUtilBotManager;
import io.github.mcengine.extension.addon.artificialintelligence.chatbot.util.ChatBotListenerUtilDB;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;

/**
 * Subcommand handler for /ai chatbot.
 * <p>
 * Handles:
 * <ul>
 *     <li>/ai chatbot set email your@email.com</li>
 *     <li>/ai chatbot &lt;platform&gt; &lt;model&gt;</li>
 * </ul>
 */
public class ChatBotCommand implements CommandExecutor {

    /**
     * Shared database instance for chatbot-specific data (e.g., player email).
     * Must be initialized before use.
     */
    public static ChatBotListenerUtilDB db;

    /**
     * Handles execution of the /ai chatbot subcommand.
     *
     * @param sender  The sender of the command.
     * @param command The command object.
     * @param label   The command alias used.
     * @param args    Arguments passed after "/ai chatbot"
     * @return true if successfully handled.
     */
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command.");
            return true;
        }

        UUID playerId = player.getUniqueId();

        // Handle: /ai chatbot set email <email>
        if (args.length >= 4 && args[1].equalsIgnoreCase("set") && args[2].equalsIgnoreCase("email")) {
            String email = args[3];
            boolean success = db.setPlayerEmail(playerId, email);
            if (success) {
                player.sendMessage(ChatColor.GREEN + "Your email has been saved successfully.");
            } else {
                player.sendMessage(ChatColor.RED + "Invalid email format or database error.");
            }
            return true;
        }

        // Validate minimum args
        if (args.length < 3) {
            player.sendMessage(ChatColor.RED + "Usage:");
            player.sendMessage(ChatColor.GRAY + "/ai chatbot {platform} {model}");
            player.sendMessage(ChatColor.GRAY + "/ai chatbot set email {your@email.com}");
            return true;
        }

        // Platform and model arguments
        String platform = args[1];

        if (args.length == 2) {
            player.sendMessage(ChatColor.RED + "Missing model name. Usage: /ai chatbot " + platform + " <model>");
            return true;
        }

        String model = args[2];

        // Get registered models
        Map<String, Map<String, ?>> registeredModels = MCEngineArtificialIntelligenceApiUtilAi.getAllModels();

        if (!registeredModels.containsKey(platform)) {
            player.sendMessage(ChatColor.RED + "Unknown platform: " + ChatColor.WHITE + platform);
            return true;
        }

        Map<String, ?> modelsForPlatform = registeredModels.get(platform);
        if (!modelsForPlatform.containsKey(model)) {
            player.sendMessage(ChatColor.RED + "Unknown model: " + ChatColor.WHITE + model +
                    ChatColor.GRAY + " for platform " + ChatColor.WHITE + platform);
            return true;
        }

        // All validations passed → Start conversation
        MCEngineArtificialIntelligenceApiUtilBotManager.setModel(player, platform, model);
        MCEngineArtificialIntelligenceApiUtilBotManager.startConversation(player);
        MCEngineArtificialIntelligenceApiUtilBotManager.activate(player);

        player.sendMessage(ChatColor.GREEN + "You are now chatting with the AI.");
        player.sendMessage(ChatColor.GRAY + "Type your message in chat. Type 'quit' to end the conversation.");
        return true;
    }
}
