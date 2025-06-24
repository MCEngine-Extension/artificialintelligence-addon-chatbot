package io.github.mcengine.extension.addon.artificialintelligence.chatbot;

import io.github.mcengine.common.artificialintelligence.MCEngineArtificialIntelligenceCommon;
import io.github.mcengine.api.artificialintelligence.extension.addon.IMCEngineArtificialIntelligenceAddOn;
import io.github.mcengine.api.mcengine.MCEngineApi;
import io.github.mcengine.api.mcengine.extension.addon.MCEngineAddOnLogger;

import io.github.mcengine.extension.addon.artificialintelligence.chatbot.api.FunctionCallingLoader;
import io.github.mcengine.extension.addon.artificialintelligence.chatbot.api.util.FunctionCallingLoaderUtilTime;
import io.github.mcengine.extension.addon.artificialintelligence.chatbot.command.ChatBotCommand;
import io.github.mcengine.extension.addon.artificialintelligence.chatbot.listener.ChatBotListener;
import io.github.mcengine.extension.addon.artificialintelligence.chatbot.tabcompleter.ChatBotTabCompleter;
import io.github.mcengine.extension.addon.artificialintelligence.chatbot.util.ChatBotListenerUtilDB;
import io.github.mcengine.extension.addon.artificialintelligence.chatbot.util.ChatBotUtil;
import io.github.mcengine.extension.addon.artificialintelligence.chatbot.util.ChatBotConfigLoader;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.util.List;

/**
 * Main class for the MCEngineChatBot AddOn.
 * <p>
 * Registers the /chatbot command, event listeners, and initializes
 * configuration and database table for email storage.
 */
public class ChatBot implements IMCEngineArtificialIntelligenceAddOn {

    /**
     * Initializes the ChatBot AddOn.
     * Called automatically by the MCEngine core plugin.
     *
     * @param plugin The Bukkit plugin instance.
     */
    @Override
    public void onLoad(Plugin plugin) {
        MCEngineAddOnLogger logger = new MCEngineAddOnLogger(plugin, "MCEngineChatBot");
        ChatBotConfigLoader.check(logger);
        FunctionCallingLoader.check(logger);
        FunctionCallingLoaderUtilTime.check(logger);

        try {
            // Initialize database table for chatbot mail
            Connection conn = MCEngineArtificialIntelligenceCommon.getApi().getDBConnection();
            ChatBotCommand.db = new ChatBotListenerUtilDB(conn, logger);

            // Create required file and config
            ChatBotUtil.createSimpleFile(plugin);
            ChatBotUtil.createConfig(plugin);

            // Register event listener
            PluginManager pluginManager = Bukkit.getPluginManager();
            pluginManager.registerEvents(new ChatBotListener(plugin, logger), plugin);

            // Reflectively access Bukkit's CommandMap
            Field commandMapField = Bukkit.getServer().getClass().getDeclaredField("commandMap");
            commandMapField.setAccessible(true);
            CommandMap commandMap = (CommandMap) commandMapField.get(Bukkit.getServer());

            // Define the /chatbot command
            Command chatbotCommand = new Command("chatbot") {
                private final ChatBotCommand handler = new ChatBotCommand();
                private final ChatBotTabCompleter completer = new ChatBotTabCompleter();

                /**
                 * Handles execution of the /chatbot command.
                 *
                 * @param sender The command sender.
                 * @param label  The command label.
                 * @param args   Command arguments.
                 * @return true if successful.
                 */
                @Override
                public boolean execute(CommandSender sender, String label, String[] args) {
                    return handler.onCommand(sender, this, label, args);
                }

                /**
                 * Handles tab-completion for the /chatbot command.
                 *
                 * @param sender The command sender.
                 * @param alias  The alias used.
                 * @param args   The current arguments.
                 * @return A list of possible completions.
                 */
                @Override
                public List<String> tabComplete(CommandSender sender, String alias, String[] args) {
                    return completer.onTabComplete(sender, this, alias, args);
                }
            };

            chatbotCommand.setDescription("Interact with the chatbot AI.");
            chatbotCommand.setUsage("/chatbot {platform} {model} {message...}");

            // Dynamically register the /chatbot command
            commandMap.register(plugin.getName().toLowerCase(), chatbotCommand);

            logger.info("Enabled successfully.");

        } catch (Exception e) {
            logger.warning("Failed to initialize ChatBot AddOn: " + e.getMessage());
            e.printStackTrace();
        }

        // Check for updates
        MCEngineApi.checkUpdate(plugin, logger.getLogger(), "[AddOn] [MCEngineChatBot] ",
            "github", "MCEngine-Extension", "artificialintelligence-addon-chat-bot",
            plugin.getConfig().getString("github.token", "null"));
    }
}
