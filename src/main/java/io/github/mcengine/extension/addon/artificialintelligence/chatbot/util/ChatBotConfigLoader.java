package io.github.mcengine.extension.addon.artificialintelligence.chatbot.util;

import io.github.mcengine.api.core.extension.logger.MCEngineExtensionLogger;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;

/**
 * Utility class to load the chatbot's custom configuration file.
 * <p>
 * Loads from: {@code plugins/YourPlugin/configs/addons/MCEngineChatBot/config.yml}
 */
public class ChatBotConfigLoader {

    /**
     * Loads the chatbot config from the custom path.
     *
     * @param plugin The plugin instance.
     * @return The loaded YAML configuration.
     */
    public static FileConfiguration getCustomConfig(Plugin plugin, String folderPath) {
        File configFile = new File(plugin.getDataFolder(), folderPath + "/config.yml");
        return YamlConfiguration.loadConfiguration(configFile);
    }
}
