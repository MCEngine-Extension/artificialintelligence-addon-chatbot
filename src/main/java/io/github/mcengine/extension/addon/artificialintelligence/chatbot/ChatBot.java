package io.github.mcengine.extension.addon.artificialintelligence.chatbot;

import io.github.mcengine.api.artificialintelligence.extension.addon.IMCEngineArtificialIntelligenceAddOn;
import io.github.mcengine.api.core.MCEngineCoreApi;
import io.github.mcengine.api.core.extension.logger.MCEngineExtensionLogger;
import io.github.mcengine.common.artificialintelligence.MCEngineArtificialIntelligenceCommon;
import io.github.mcengine.extension.addon.artificialintelligence.chatbot.command.ChatBotCommand;
import io.github.mcengine.extension.addon.artificialintelligence.chatbot.listener.ChatBotListener;
import io.github.mcengine.extension.addon.artificialintelligence.chatbot.tabcompleter.ChatBotTabCompleter;
import io.github.mcengine.extension.addon.artificialintelligence.chatbot.util.ChatBotUtil;
import io.github.mcengine.extension.addon.artificialintelligence.chatbot.database.ChatBotDB;
import io.github.mcengine.extension.addon.artificialintelligence.chatbot.database.mysql.ChatBotDBMySQL;
import io.github.mcengine.extension.addon.artificialintelligence.chatbot.database.postgresql.ChatBotDBPostgreSQL;
import io.github.mcengine.extension.addon.artificialintelligence.chatbot.database.sqlite.ChatBotDBSQLite;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

import java.io.File;

/**
 * Main class for the MCEngineChatBot AddOn.
 *
 * <p>Registers the {@code chatbot} subcommand of /ai and event listeners using the MCEngine dispatcher system.
 * Also initializes the chatbot configuration and database setup.</p>
 */
public class ChatBot implements IMCEngineArtificialIntelligenceAddOn {

    /**
     * The relative path where the ChatBot config is stored.
     */
    private final String folderPath = "extensions/addons/configs/MCEngineChatBot";

    /**
     * Logger for initialization and runtime messages specific to this AddOn.
     */
    private MCEngineExtensionLogger logger;

    /**
     * Database accessor for chatbot-specific persistence (e.g., player emails).
     */
    private ChatBotDB chatBotDB;

    /**
     * Initializes the ChatBot AddOn.
     * Called automatically by the MCEngine core plugin.
     *
     * @param plugin The Bukkit plugin instance.
     */
    @Override
    public void onLoad(Plugin plugin) {
        this.logger = new MCEngineExtensionLogger(plugin, "AddOn", "MCEngineChatBot");

        ChatBotUtil.createConfig(plugin, folderPath);

        File configFile = new File(plugin.getDataFolder(), folderPath + "/config.yml");
        YamlConfiguration config = YamlConfiguration.loadConfiguration(configFile);
        String licenseType = config.getString("license", "free");

        if (!"free".equalsIgnoreCase(licenseType)) {
            logger.warning("License is not 'free'. Disabling ChatBot AddOn.");
            return;
        }

        try {
            // Initialize database for chatbot usage (emails, logs, etc.) with dialect selection
            String dbType;
            try {
                dbType = plugin.getConfig().getString("database.type", "sqlite");
            } catch (Throwable t) {
                dbType = "sqlite";
            }

            switch (dbType == null ? "sqlite" : dbType.toLowerCase()) {
                case "mysql" -> chatBotDB = new ChatBotDBMySQL(logger);
                case "postgresql", "postgres" -> chatBotDB = new ChatBotDBPostgreSQL(logger);
                case "sqlite" -> chatBotDB = new ChatBotDBSQLite(logger);
                default -> {
                    logger.warning("Unknown database.type='" + dbType + "', defaulting to SQLite for ChatBot.");
                    chatBotDB = new ChatBotDBSQLite(logger);
                }
            }
            chatBotDB.ensureSchema();

            // Expose DB to command/flows
            ChatBotCommand.db = chatBotDB;

            // Register events
            PluginManager pluginManager = Bukkit.getPluginManager();
            pluginManager.registerEvents(new ChatBotListener(plugin, folderPath, logger), plugin);

            // Register dispatcher command under the "chatbot" subcommand of /ai
            String namespace = "ai";
            String subcommand = "chatbot";

            MCEngineArtificialIntelligenceCommon api = MCEngineArtificialIntelligenceCommon.getApi();
            api.registerSubCommand(namespace, subcommand, new ChatBotCommand());
            api.registerSubTabCompleter(namespace, subcommand, new ChatBotTabCompleter());

            logger.info("ChatBot dispatcher subcommand registered successfully.");

        } catch (Exception e) {
            logger.warning("Failed to initialize ChatBot AddOn: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Sets the identifier for this ChatBot AddOn.
     *
     * @param id the unique identifier string
     */
    @Override
    public void setId(String id) {
        MCEngineCoreApi.setId("mcengine-chatbot");
    }

    /**
     * Called when the plugin is disabled or the AddOn is being unloaded.
     *
     * @param plugin the Bukkit plugin instance
     */
    @Override
    public void onDisload(Plugin plugin) {
        // No custom shutdown logic needed.
    }
}
