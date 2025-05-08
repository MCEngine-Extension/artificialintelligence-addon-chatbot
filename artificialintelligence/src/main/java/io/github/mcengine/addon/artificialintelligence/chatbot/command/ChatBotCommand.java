package io.github.mcengine.addon.artificialintelligence.chatbot.command;

import io.github.mcengine.api.artificialintelligence.MCEngineArtificialIntelligenceApi;
import io.github.mcengine.api.artificialintelligence.addon.IMCEngineArtificialIntelligenceAddOn;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Command to interact with chatbot: /chatbot {platform} {model}
 */
public class ChatBotCommand implements CommandExecutor {

    /**
     * Executes the /chatbot command to interact with the AI.
     *
     * @param sender  The sender of the command.
     * @param command The command that was executed.
     * @param label   The alias of the command used.
     * @param args    The arguments passed with the command.
     * @return true if the command was processed; false otherwise.
     */
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cOnly players can use this command.");
            return true;
        }

        if (args.length < 3) {
            sender.sendMessage("§cUsage: /chatbot {platform} {model} {message...}");
            return true;
        }

        String platform = args[0];
        String model = args[1];
        String message = String.join(" ", java.util.Arrays.copyOfRange(args, 2, args.length));

        MCEngineArtificialIntelligenceApi api = MCEngineArtificialIntelligenceApi.getApi();
        String response;

        try {
            response = api.getResponse(platform, model, message);
        } catch (Exception e) {
            sender.sendMessage("§cFailed to get response from chatbot: " + e.getMessage());
            return true;
        }

        sender.sendMessage("§e[ChatBot]§r " + response);
        return true;
    }
}
