package io.github.mcengine.addon.artificialintelligence.chatbot.command;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Command to interact with chatbot: /chatbot {platform} {model} {message...}
 */
public class ChatBotCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cOnly players can use this command.");
            return true;
        }

        if (args.length < 3) {
            player.sendMessage("§cUsage: /chatbot {platform} {model} {message...}");
            return true;
        }

        String platform = args[0];
        String model = args[1];
        String message = String.join(" ", java.util.Arrays.copyOfRange(args, 2, args.length));

        player.sendMessage("§7[ChatBot] Thinking...");

        new ChatBotTask(Bukkit.getPluginManager().getPlugin("MCEngineArtificialIntelligence"),
                        player, platform, model, message)
                .runTaskAsynchronously(Bukkit.getPluginManager().getPlugin("MCEngineArtificialIntelligence"));
        return true;
    }
}
