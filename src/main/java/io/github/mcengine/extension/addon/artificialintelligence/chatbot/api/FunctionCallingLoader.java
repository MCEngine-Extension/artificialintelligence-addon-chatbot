package io.github.mcengine.extension.addon.artificialintelligence.chatbot.api;

import io.github.mcengine.api.core.extension.addon.MCEngineAddOnLogger;
import io.github.mcengine.extension.addon.artificialintelligence.chatbot.api.json.FunctionCallingJson;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.*;
import java.util.function.Supplier;
import java.util.regex.Pattern;

import static io.github.mcengine.extension.addon.artificialintelligence.chatbot.api.util.FunctionCallingEntity.*;
import static io.github.mcengine.extension.addon.artificialintelligence.chatbot.api.util.FunctionCallingItem.*;
import static io.github.mcengine.extension.addon.artificialintelligence.chatbot.api.util.FunctionCallingLoaderUtilTime.*;
import static io.github.mcengine.extension.addon.artificialintelligence.chatbot.api.util.FunctionCallingWorld.*;

/**
 * Loads and handles matching of function calling rules for the MCEngineChatBot plugin.
 * Supports placeholder replacement, dynamic fuzzy matching using regex, and time zone formatting.
 */
public class FunctionCallingLoader {

    /** The main plugin instance for file lookups and context. */
    private final Plugin plugin;

    /** List of all rules loaded from `.json` files, used to determine chatbot responses. */
    private final List<FunctionRule> mergedRules = new ArrayList<>();

    /**
     * Constructs the loader and loads rules from all `.json` files in the configured directory.
     * Logs the number of rules loaded.
     *
     * @param plugin     The plugin instance used for locating the data folder.
     * @param folderPath The folder path relative to the plugin data directory.
     * @param logger     The logger instance used for logging info to console.
     */
    public FunctionCallingLoader(Plugin plugin, String folderPath, MCEngineAddOnLogger logger) {
        this.plugin = plugin;
        IFunctionCallingLoader loader = new FunctionCallingJson(
                new java.io.File(plugin.getDataFolder(), folderPath + "/data/")
        );
        mergedRules.addAll(loader.loadFunctionRules());
        logger.info("Loaded " + mergedRules.size() + " function rules.");
    }

    /**
     * Verifies this class is loaded and outputs a test log message.
     *
     * @param logger The logger to use for output.
     */
    public static void check(MCEngineAddOnLogger logger) {
        logger.info("Class: FunctionCallingLoader is loaded.");
    }

    /**
     * Matches player input against known rules using regex-based fuzzy matching.
     * If any rule matches, the response will be dynamically filled with placeholders and returned.
     *
     * @param player The player who sent the input.
     * @param input  The raw user input text.
     * @return A list of response strings that matched and were resolved with placeholders.
     */
    public List<String> match(Player player, String input) {
        List<String> results = new ArrayList<>();
        String trimmedInput = input.trim();

        for (FunctionRule rule : mergedRules) {
            for (String raw : rule.match) {
                String pattern = convertToRegex(raw);
                Pattern regex = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
                if (regex.matcher(trimmedInput).find()) {
                    String resolved = applyPlaceholders(rule.response, player);
                    results.add(resolved);
                    break;
                }
            }
        }
        return results;
    }

    /**
     * Converts a plain user-friendly match string into a basic regex pattern for fuzzy matching.
     *
     * @param text The plain text pattern from JSON.
     * @return A regex pattern string.
     */
    private String convertToRegex(String text) {
        String[] words = text.trim().toLowerCase().split("\\s+");
        return ".*" + String.join(".*", words) + ".*";
    }

    /**
     * Replaces placeholders in a chatbot response with real-time values from the player or server.
     * Optimization: Uses a map of placeholder keys and value suppliers for easier maintenance and better performance.
     *
     * @param response The raw response containing placeholders.
     * @param player   The player whose data is used for substitution.
     * @return A fully resolved string with all placeholders replaced.
     */
    private String applyPlaceholders(String response, Player player) {
        World world = player.getWorld();

        // --- Entity Placeholder Map (auto-generated would be best, but explicit for clarity) ---
        Map<String, Supplier<String>> placeholders = new LinkedHashMap<>();

        // Count & Detail placeholders for all nearby entities
        placeholders.put("{nearby_entities_count}", () -> getNearbyEntities(plugin, player, 20));
        placeholders.put("{nearby_entities_detail}", () -> getNearbyEntities(plugin, player, 20));

        // Individual entity types (count and detail)
        String[] entityTypes = {
            "allay", "armadillo", "axolotl", "bat", "bee", "blaze", "bogged", "breeze",
            "camel", "cat", "cave_spider", "chicken", "cod", "cow", "creeper", "dolphin",
            "donkey", "drowned", "elder_guardian", "ender_dragon", "endermite", "evoker",
            "fox", "frog", "ghast", "glow_squid", "goat", "guardian", "hoglin", "horse",
            "husk", "illusioner", "iron_golem", "llama", "magma_cube", "mooshroom", "mule",
            "ocelot", "panda", "parrot", "phantom", "pig", "piglin", "piglin_brute",
            "pillager", "polar_bear", "pufferfish", "rabbit", "ravager", "salmon", "sheep",
            "shulker", "silverfish", "skeleton", "skeleton_horse", "slime", "sniffer",
            "snow_golem", "spider", "squid", "stray", "strider", "trader_llama",
            "tropical_fish", "turtle", "vex", "vindicator", "warden", "witch", "wither",
            "wither_skeleton", "wolf", "zoglin", "zombie", "zombie_horse", "zombie_villager",
            "zombified_piglin"
        };
        for (String type : entityTypes) {
            placeholders.put("{nearby_" + type + "_count}", () -> getNearbyEntities(plugin, player, type, 20));
            placeholders.put("{nearby_" + type + "_detail}", () -> getNearbyEntities(plugin, player, type, 20));
        }

        // --- Player-related placeholders (sorted) ---
        placeholders.put("{item_in_hand}", () -> getItemInHandDetails(player));
        placeholders.put("{player_displayname}", player::getDisplayName);
        placeholders.put("{player_exp_level}", () -> String.valueOf(player.getLevel()));
        placeholders.put("{player_food_level}", () -> String.valueOf(player.getFoodLevel()));
        placeholders.put("{player_gamemode}", () -> player.getGameMode().name());
        placeholders.put("{player_health}", () -> String.valueOf(player.getHealth()));
        placeholders.put("{player_inventory}", () -> getPlayerInventoryDetails(player));
        placeholders.put("{player_ip}", () -> player.getAddress() != null ? player.getAddress().getAddress().getHostAddress() : "unknown");
        placeholders.put("{player_location}", () -> String.format("X: %.1f, Y: %.1f, Z: %.1f",
                player.getLocation().getX(), player.getLocation().getY(), player.getLocation().getZ()));
        placeholders.put("{player_max_health}", () -> String.valueOf(player.getMaxHealth()));
        placeholders.put("{player_name}", player::getName);
        placeholders.put("{player_uuid}", () -> player.getUniqueId().toString());
        placeholders.put("{player_uuid_short}", () -> player.getUniqueId().toString().split("-")[0]);
        placeholders.put("{player_world}", () -> world.getName());

        // --- World and environment placeholders (sorted) ---
        placeholders.put("{world_difficulty}", () -> world.getDifficulty().name());
        placeholders.put("{world_entity_count}", () -> getSafeEntityCount(plugin, world));
        placeholders.put("{world_loaded_chunks}", () -> String.valueOf(world.getLoadedChunks().length));
        placeholders.put("{world_seed}", () -> String.valueOf(world.getSeed()));
        placeholders.put("{world_time}", () -> String.valueOf(world.getTime()));
        placeholders.put("{world_weather}", () -> world.hasStorm() ? "Raining" : "Clear");

        // --- Static time zones ---
        placeholders.put("{time_gmt}", () -> getFormattedTime(TimeZone.getTimeZone("GMT")));
        placeholders.put("{time_server}", () -> getFormattedTime(TimeZone.getDefault()));
        placeholders.put("{time_utc}", () -> getFormattedTime(TimeZone.getTimeZone("UTC")));

        // --- Named zones ---
        Map<String, String> namedZones = Map.ofEntries(
                Map.entry("{time_bangkok}", getFormattedTime("Asia/Bangkok")),
                Map.entry("{time_berlin}", getFormattedTime("Europe/Berlin")),
                Map.entry("{time_london}", getFormattedTime("Europe/London")),
                Map.entry("{time_los_angeles}", getFormattedTime("America/Los_Angeles")),
                Map.entry("{time_new_york}", getFormattedTime("America/New_York")),
                Map.entry("{time_paris}", getFormattedTime("Europe/Paris")),
                Map.entry("{time_singapore}", getFormattedTime("Asia/Singapore")),
                Map.entry("{time_sydney}", getFormattedTime("Australia/Sydney")),
                Map.entry("{time_tokyo}", getFormattedTime("Asia/Tokyo")),
                Map.entry("{time_toronto}", getFormattedTime("America/Toronto"))
        );

        // --- Apply simple placeholders ---
        for (Map.Entry<String, Supplier<String>> entry : placeholders.entrySet()) {
            response = response.replace(entry.getKey(), entry.getValue().get());
        }

        // --- Apply named time zones ---
        for (Map.Entry<String, String> entry : namedZones.entrySet()) {
            response = response.replace(entry.getKey(), entry.getValue());
        }

        // --- Apply {time_gmt+X:00}, {time_utc+X:00}, etc. for -12..+14 (with :00, :30, :45) ---
        for (int hour = -12; hour <= 14; hour++) {
            for (int min : new int[]{0, 30, 45}) {
                String utcLabel = getZoneLabel("utc", hour, min);
                String gmtLabel = getZoneLabel("gmt", hour, min);
                TimeZone tz = TimeZone.getTimeZone(String.format("GMT%+03d:%02d", hour, min));
                String time = getFormattedTime(tz);
                response = response.replace(utcLabel, time);
                response = response.replace(gmtLabel, time);
            }
        }

        return response;
    }
}
