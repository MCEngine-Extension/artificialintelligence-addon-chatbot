package io.github.mcengine.extension.addon.artificialintelligence.chatbot.command;

import io.github.mcengine.api.artificialintelligence.util.MCEngineArtificialIntelligenceApiUtilBotManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Command to start an AI chat session.
 * Usage: /chatbot {platform} {model}
 */
public class ChatBotCommand implements CommandExecutor {

    /**
     * Handles the /chatbot command to initiate a conversation.
     *
     * @param sender  The command sender.
     * @param command The command object.
     * @param label   The label used.
     * @param args    The arguments provided.
     * @return true if the command was handled.
     */
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cOnly players can use this command.");
            return true;
        }

        if (args.length < 2) {
            player.sendMessage("§cUsage: /chatbot {platform} {model}");
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
