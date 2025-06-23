package io.github.mcengine.extension.addon.artificialintelligence.chatbot.command;

import io.github.mcengine.api.artificialintelligence.util.MCEngineArtificialIntelligenceApiUtilBotManager;
import io.github.mcengine.extension.addon.artificialintelligence.chatbot.util.ChatBotListenerUtilDB;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * Handles the execution of the /chatbot command, allowing players
 * to either start a conversation with the AI or register their email address.
 *
 * Supported command usage:
 * <ul>
 *     <li>/chatbot set email your@email.com</li>
 *     <li>/chatbot {platform} {model}</li>
 * </ul>
 */
public class ChatBotCommand implements CommandExecutor {

    /**
     * Static reference to the database utility class.
     * Must be initialized in the plugin's main class.
     */
    public static ChatBotListenerUtilDB db;

    /**
     * Executes the /chatbot command.
     *
     * @param sender  The source of the command.
     * @param command The command object.
     * @param label   The alias of the command.
     * @param args    The passed command arguments.
     * @return true if the command was processed successfully.
     */
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cOnly players can use this command.");
            return true;
        }

        UUID playerId = player.getUniqueId();

        // /chatbot set email your@email.com
        if (args.length >= 3 && args[0].equalsIgnoreCase("set") && args[1].equalsIgnoreCase("email")) {
            String email = args[2];
            boolean success = db.setPlayerEmail(playerId, email);
            if (success) {
                player.sendMessage("§aYour email has been saved successfully.");
            } else {
                player.sendMessage("§cInvalid email format or database error.");
            }
            return true;
        }

        // /chatbot {platform} {model}
        if (args.length < 2) {
            player.sendMessage("§cUsage:");
            player.sendMessage("§7/chatbot {platform} {model}");
            player.sendMessage("§7/chatbot set email {your@email.com}");
            return true;
        }

        String platform = args[0];
        String model = args[1];

        MCEngineArtificialIntelligenceApiUtilBotManager.setModel(player, platform, model);
        MCEngineArtificialIntelligenceApiUtilBotManager.startConversation(player);
        MCEngineArtificialIntelligenceApiUtilBotManager.activate(player);

        player.sendMessage("§aYou are now chatting with the AI.");
        player.sendMessage("§7Type your message in chat. Type 'quit' to end the conversation.");
        return true;
    }
}
