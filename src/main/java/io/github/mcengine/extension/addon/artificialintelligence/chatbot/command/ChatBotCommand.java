package io.github.mcengine.extension.addon.artificialintelligence.chatbot.command;

import io.github.mcengine.api.artificialintelligence.util.MCEngineArtificialIntelligenceApiUtilBotManager;
import io.github.mcengine.extension.addon.artificialintelligence.chatbot.util.ChatBotListenerUtilDB;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Command to start an AI chat session or set player email.
 * <p>
 * Usage:
 * <ul>
 *     <li>/chatbot {platform} {model} - starts a conversation</li>
 *     <li>/chatbot set email {address} - sets your email address</li>
 * </ul>
 */
public class ChatBotCommand implements CommandExecutor {

    /**
     * Handles the /chatbot command to initiate or manage an AI session.
     *
     * @param sender  The command sender.
     * @param command The command object.
     * @param label   The command label.
     * @param args    Command arguments.
     * @return true if the command was processed.
     */
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cOnly players can use this command.");
            return true;
        }

        // /chatbot set email your@email.com
        if (args.length >= 3 && args[0].equalsIgnoreCase("set") && args[1].equalsIgnoreCase("email")) {
            String email = args[2];
            ChatBotListenerUtilDB.setPlayerEmail(player, email);
            return true;
        }

        // /chatbot platform model
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
