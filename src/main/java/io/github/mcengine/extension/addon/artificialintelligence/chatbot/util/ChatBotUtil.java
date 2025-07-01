package io.github.mcengine.extension.addon.artificialintelligence.chatbot.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.bukkit.plugin.Plugin;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Utility class for creating default configuration and data files
 * for the MCEngineChatBot plugin.
 */
public class ChatBotUtil {

    /**
     * Creates the default `data.json` file containing sample chatbot rules.
     * Skips creation if the target folder already exists.
     * The file is saved to: {@code <plugin_data_folder>/<folderPath>/data/data.json}
     *
     * @param plugin     The plugin instance used to resolve the data folder.
     * @param folderPath The folder path relative to the plugin data directory.
     */
    public static void createSimpleFile(Plugin plugin, String folderPath) {
        File dataFolder = new File(plugin.getDataFolder(), folderPath + "/data/");

        // If folder already exists, skip file creation
        if (dataFolder.exists()) {
            return;
        }

        // Attempt to create directory
        if (!dataFolder.mkdirs()) {
            System.err.println("Failed to create directory: " + dataFolder.getAbsolutePath());
            return;
        }

        /**
         * List of chatbot example responses matched to each placeholder.
         * This serves as a quick reference and default behavior for plugin users.
         */
        List<Map<String, Object>> data = List.of(
            // Item
            Map.of("match", Arrays.asList("What is in my hand?", "Show my held item"), "response", "You are holding: {item_in_hand}"),

            // Player
            Map.of("match", Arrays.asList("What is my display name?", "Show display name"), "response", "Your display name is {player_displayname}."),
            Map.of("match", Arrays.asList("How much XP do I have?", "What is my level?"), "response", "Your experience level is {player_exp_level}."),
            Map.of("match", Arrays.asList("How hungry am I?", "What is my food level?"), "response", "Your food level is {player_food_level}."),
            Map.of("match", Arrays.asList("What mode am I in?", "Tell me my game mode"), "response", "You are in {player_gamemode} mode."),
            Map.of("match", Arrays.asList("How much health do I have?", "Tell me my health"), "response", "You have {player_health} health."),
            Map.of("match", Arrays.asList("What is in my inventory?", "List my items"), "response", "Inventory contents:\n{player_inventory}"),
            Map.of("match", Arrays.asList("What is my IP address?", "Tell me my IP"), "response", "Your IP address is {player_ip}."),
            Map.of("match", Arrays.asList("Where am I?", "Tell me my location"), "response", "You are at {player_location} in world {player_world}."),
            Map.of("match", Arrays.asList("What is my max health?", "Max HP"), "response", "Your max health is {player_max_health}."),
            Map.of("match", Arrays.asList("What is my name?", "Who am I?"), "response", "Your name is {player_name}."),
            Map.of("match", Arrays.asList("What is my UUID?", "Tell me my player ID"), "response", "Your UUID is {player_uuid}."),
            Map.of("match", Arrays.asList("What is my short UUID?", "Shorten my UUID"), "response", "Short UUID: {player_uuid_short}"),
            Map.of("match", Arrays.asList("What world am I in?", "Tell me my world"), "response", "You are in world: {player_world}."),

            // World
            Map.of("match", Arrays.asList("How hard is this world?", "Tell me world difficulty"), "response", "World difficulty: {world_difficulty}"),
            Map.of("match", Arrays.asList("How many entities are in the world?"), "response", "Entities in world: {world_entity_count}"),
            Map.of("match", Arrays.asList("How many chunks are loaded?"), "response", "Loaded chunks: {world_loaded_chunks}"),
            Map.of("match", Arrays.asList("What is the seed?", "World seed?"), "response", "World seed: {world_seed}"),
            Map.of("match", Arrays.asList("What time is it in-game?", "Tell me Minecraft time"), "response", "World time: {world_time}"),
            Map.of("match", Arrays.asList("What is the weather like?", "Current weather?"), "response", "World weather: {world_weather}"),

            // Static time zones
            Map.of("match", Arrays.asList("What is the server time?", "Current server time"), "response", "Server time is {time_server}."),
            Map.of("match", Arrays.asList("What is the UTC time?", "Tell me UTC time"), "response", "UTC time is {time_utc}."),
            Map.of("match", Arrays.asList("What is GMT time?", "Time in GMT?"), "response", "GMT time is {time_gmt}."),

            // Named time zones
            Map.of("match", Arrays.asList("Bangkok time?", "What time is it in Bangkok?"), "response", "Bangkok time is {time_bangkok}."),
            Map.of("match", Arrays.asList("Berlin time?", "What time is it in Berlin?"), "response", "Berlin time is {time_berlin}."),
            Map.of("match", Arrays.asList("London time?", "What time is it in London?"), "response", "London time is {time_london}."),
            Map.of("match", Arrays.asList("LA time?", "What time is it in Los Angeles?"), "response", "Los Angeles time is {time_los_angeles}."),
            Map.of("match", Arrays.asList("New York time?", "What time is it in New York?"), "response", "New York time is {time_new_york}."),
            Map.of("match", Arrays.asList("Paris time?", "What time is it in Paris?"), "response", "Paris time is {time_paris}."),
            Map.of("match", Arrays.asList("Singapore time?", "What time is it in Singapore?"), "response", "Singapore time is {time_singapore}."),
            Map.of("match", Arrays.asList("Sydney time?", "What time is it in Sydney?"), "response", "Sydney time is {time_sydney}."),
            Map.of("match", Arrays.asList("Tokyo time?", "What time is it in Tokyo?"), "response", "Tokyo time is {time_tokyo}."),
            Map.of("match", Arrays.asList("Toronto time?", "What time is it in Toronto?"), "response", "Toronto time is {time_toronto}."),

            // Example for UTC offset
            Map.of("match", Arrays.asList("What is time in GMT+7?", "Time in UTC+7?"), "response", "Time in GMT+7 is {time_gmt_plus_07_00}."),

            // List placeholders
            Map.of("match", Arrays.asList("Tell me all placeholders", "Show me the AI variables"), "response", 
                   "Placeholders: {player_name}, {player_uuid}, {player_displayname}, {player_ip}, {player_gamemode}, "
                   + "{player_health}, {player_max_health}, {player_food_level}, {player_exp_level}, {player_location}, "
                   + "{player_world}, {item_in_hand}, {player_inventory}, {time_server}, {time_utc}, {time_gmt}, "
                   + "{time_bangkok}, {time_berlin}, {time_london}, {time_los_angeles}, {time_new_york}, {time_paris}, "
                   + "{time_singapore}, {time_sydney}, {time_tokyo}, {time_toronto}, {time_gmt_plus_07_00}")
        );

        // Write data to JSON file
        File jsonFile = new File(dataFolder, "data.json");
        try (FileWriter writer = new FileWriter(jsonFile)) {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            gson.toJson(data, writer);
            System.out.println("Created default chatbot data: " + jsonFile.getAbsolutePath());
        } catch (IOException e) {
            System.err.println("Failed to write chatbot data: " + e.getMessage());
            e.printStackTrace();
        }
    }

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

        config.set("token.type", "server");
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
