package io.github.mcengine.extension.addon.artificialintelligence.chatbot.api;

import io.github.mcengine.api.core.extension.addon.MCEngineAddOnLogger;
import io.github.mcengine.extension.addon.artificialintelligence.chatbot.api.json.FunctionCallingJson;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.*;
import java.util.regex.Pattern;

import static io.github.mcengine.extension.addon.artificialintelligence.chatbot.api.util.FunctionCallingItem.*;
import static io.github.mcengine.extension.addon.artificialintelligence.chatbot.api.util.FunctionCallingLoaderUtilTime.*;

/**
 * Loads and handles matching of function calling rules for the MCEngineChatBot plugin.
 * Supports placeholder replacement, dynamic fuzzy matching using regex, and time zone formatting.
 */
public class FunctionCallingLoader {

    /**
     * List of all rules loaded from `.json` files, used to determine chatbot responses.
     */
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
     *
     * @param response The raw response containing placeholders.
     * @param player   The player whose data is used for substitution.
     * @return A fully resolved string with all placeholders replaced.
     */
    private String applyPlaceholders(String response, Player player) {
        World world = player.getWorld();

        response = response
                // Player-related placeholders (sorted A–Z)
                .replace("{item_in_hand}", getItemInHandDetails(player))
                .replace("{player_displayname}", player.getDisplayName())
                .replace("{player_exp_level}", String.valueOf(player.getLevel()))
                .replace("{player_food_level}", String.valueOf(player.getFoodLevel()))
                .replace("{player_gamemode}", player.getGameMode().name())
                .replace("{player_health}", String.valueOf(player.getHealth()))
                .replace("{player_inventory}", getPlayerInventoryDetails(player))
                .replace("{player_ip}", player.getAddress() != null ? player.getAddress().getAddress().getHostAddress() : "unknown")
                .replace("{player_location}", String.format("X: %.1f, Y: %.1f, Z: %.1f",
                        player.getLocation().getX(),
                        player.getLocation().getY(),
                        player.getLocation().getZ()))
                .replace("{player_max_health}", String.valueOf(player.getMaxHealth()))
                .replace("{player_name}", player.getName())
                .replace("{player_uuid}", player.getUniqueId().toString())
                .replace("{player_uuid_short}", player.getUniqueId().toString().split("-")[0])
                .replace("{player_world}", world.getName())

                // World and environment placeholders (sorted A–Z)
                .replace("{world_difficulty}", world.getDifficulty().name())
                .replace("{world_entity_count}", String.valueOf(world.getEntities().size()))
                .replace("{world_loaded_chunks}", String.valueOf(world.getLoadedChunks().length))
                .replace("{world_seed}", String.valueOf(world.getSeed()))
                .replace("{world_time}", String.valueOf(world.getTime()))
                .replace("{world_weather}", world.hasStorm() ? "Raining" : "Clear")

                // Static time zones
                .replace("{time_gmt}", getFormattedTime(TimeZone.getTimeZone("GMT")))
                .replace("{time_server}", getFormattedTime(TimeZone.getDefault()))
                .replace("{time_utc}", getFormattedTime(TimeZone.getTimeZone("UTC")));

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

        for (Map.Entry<String, String> entry : namedZones.entrySet()) {
            response = response.replace(entry.getKey(), entry.getValue());
        }

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
