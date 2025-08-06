package io.github.mcengine.extension.addon.artificialintelligence.chatbot.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.bukkit.plugin.Plugin;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

/**
 * Utility class for creating default configuration and data files
 * for the MCEngineChatBot plugin.
 */
public class ChatBotUtil {

    /**
     * Creates a default `config.yml` file for the chatbot plugin.
     * This config sets the token type to "server".
     * File is saved to: {@code <plugin_data_folder>/<folderPath>/config.yml}
     *
     * @param plugin     The plugin instance used to determine the data folder.
     * @param folderPath The folder path relative to the plugin data directory.
     */
    public static void createConfig(Plugin plugin, String folderPath) {
        File configFile = new File(plugin.getDataFolder(), folderPath + "/config.yml");

        // Skip creation if config already exists
        if (configFile.exists()) return;

        // Ensure parent directory exists
        File configDir = configFile.getParentFile();
        if (!configDir.exists() && !configDir.mkdirs()) {
            System.err.println("Failed to create config directory: " + configDir.getAbsolutePath());
            return;
        }

        YamlConfiguration config = new YamlConfiguration();
        config.options().header(
            "Token Type Options:\n" +
            "  - \"server\": Uses the default token from the main config file.\n" +
            "  - \"player\": Uses the player's personal token.\n\n" +
            "Mail Configuration:\n" +
            "  mail.enable: Whether to send emails when the player types \"quit\".\n" +
            "  mail.type: Options are \"gmail\" or \"outlook\".\n" +
            "  mail.email: The sender's email address.\n" +
            "  mail.password: App password for SMTP login.\n" +
            "  mail.owner: Optional fallback address (currently not used)."
        );

        config.set("license", "free");
        config.set("token.type", "server");
        config.set("ai.system.prompt", "You're an AI assistant designed to help players in this Minecraft game!");
        config.set("mail.enable", false);
        config.set("mail.type", "gmail");
        config.set("mail.email", "your-email@gmail.com");
        config.set("mail.password", "your-app-password");
        config.set("mail.owner", "owner@example.com");

        try {
            config.save(configFile);
            System.out.println("Created default chatbot config: " + configFile.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
