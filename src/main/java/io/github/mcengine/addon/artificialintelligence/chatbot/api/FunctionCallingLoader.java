package io.github.mcengine.addon.artificialintelligence.chatbot.api;

import io.github.mcengine.addon.artificialintelligence.chatbot.api.json.FunctionCallingJson;
import io.github.mcengine.addon.artificialintelligence.chatbot.api.util.FunctionCallingLoaderUtilTime;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.*;

/**
 * Loads and handles matching of function calling rules for the MCEngineChatBot plugin.
 * Supports placeholder replacement and time zone formatting in responses.
 */
public class FunctionCallingLoader {

    private final List<FunctionRule> mergedRules = new ArrayList<>();

    /**
     * Constructs the loader and loads rules from all `.json` files in the configured directory.
     * Logs the number of rules loaded.
     *
     * @param plugin The plugin instance used for locating the data folder and logging.
     */
    public FunctionCallingLoader(Plugin plugin) {
        IFunctionCallingLoader loader = new FunctionCallingJson(
                new java.io.File(plugin.getDataFolder(), "configs/addons/MCEngineChatBot/data/")
        );
        mergedRules.addAll(loader.loadFunctionRules());

        plugin.getLogger().info("Loaded " + mergedRules.size() + " function rules.");
    }

    /**
     * Matches the input string against known function rules for the given player.
     * Performs case-insensitive fuzzy matching and applies dynamic placeholders.
     *
     * @param player The player providing the input (used for placeholder replacement).
     * @param input  The user-provided input string to match against.
     * @return A list of resolved responses from matched rules.
     */
    public List<String> match(Player player, String input) {
        List<String> results = new ArrayList<>();
        String lowerInput = input.toLowerCase().trim();

        for (FunctionRule rule : mergedRules) {
            for (String pattern : rule.match) {
                String lowerPattern = pattern.toLowerCase();
                if (lowerInput.contains(lowerPattern) || lowerPattern.contains(lowerInput)) {
                    String resolved = applyPlaceholders(rule.response, player);
                    results.add(resolved);
                    break;
                }
            }
        }

        return results;
    }

    /**
     * Applies placeholders to a rule's response string based on the provided player's data and various time zones.
     *
     * @param response The raw response string containing placeholders.
     * @param player   The player whose data will be used for placeholder replacement.
     * @return The formatted response with all placeholders replaced.
     */
    private String applyPlaceholders(String response, Player player) {
        response = response
                // Player info
                .replace("{player_name}", player.getName())
                .replace("{player_uuid}", player.getUniqueId().toString())
                .replace("{player_uuid_short}", player.getUniqueId().toString().split("-")[0])
                .replace("{player_displayname}", player.getDisplayName())
                .replace("{player_ip}", player.getAddress() != null ? player.getAddress().getAddress().getHostAddress() : "unknown")
                .replace("{player_gamemode}", player.getGameMode().name())
                .replace("{player_world}", player.getWorld().getName())
                .replace("{player_location}", String.format("X: %.1f, Y: %.1f, Z: %.1f",
                        player.getLocation().getX(),
                        player.getLocation().getY(),
                        player.getLocation().getZ()))
                .replace("{player_health}", String.valueOf(player.getHealth()))
                .replace("{player_max_health}", String.valueOf(player.getMaxHealth()))
                .replace("{player_food_level}", String.valueOf(player.getFoodLevel()))
                .replace("{player_exp_level}", String.valueOf(player.getLevel()))

                // Static time zones
                .replace("{time_server}", FunctionCallingLoaderUtilTime.getFormattedTime(TimeZone.getDefault()))
                .replace("{time_utc}", FunctionCallingLoaderUtilTime.getFormattedTime(TimeZone.getTimeZone("UTC")))
                .replace("{time_gmt}", FunctionCallingLoaderUtilTime.getFormattedTime(TimeZone.getTimeZone("GMT")));

        // Named time zones
        Map<String, String> namedZones = Map.ofEntries(
                Map.entry("{time_new_york}", FunctionCallingLoaderUtilTime.getFormattedTime("America/New_York")),
                Map.entry("{time_london}", FunctionCallingLoaderUtilTime.getFormattedTime("Europe/London")),
                Map.entry("{time_tokyo}", FunctionCallingLoaderUtilTime.getFormattedTime("Asia/Tokyo")),
                Map.entry("{time_bangkok}", FunctionCallingLoaderUtilTime.getFormattedTime("Asia/Bangkok")),
                Map.entry("{time_sydney}", FunctionCallingLoaderUtilTime.getFormattedTime("Australia/Sydney")),
                Map.entry("{time_paris}", FunctionCallingLoaderUtilTime.getFormattedTime("Europe/Paris")),
                Map.entry("{time_berlin}", FunctionCallingLoaderUtilTime.getFormattedTime("Europe/Berlin")),
                Map.entry("{time_singapore}", FunctionCallingLoaderUtilTime.getFormattedTime("Asia/Singapore")),
                Map.entry("{time_los_angeles}", FunctionCallingLoaderUtilTime.getFormattedTime("America/Los_Angeles")),
                Map.entry("{time_toronto}", FunctionCallingLoaderUtilTime.getFormattedTime("America/Toronto"))
        );

        for (Map.Entry<String, String> entry : namedZones.entrySet()) {
            response = response.replace(entry.getKey(), entry.getValue());
        }

        // UTC/GMT offsets from -12:00 to +14:00
        for (int hour = -12; hour <= 14; hour++) {
            for (int min : new int[]{0, 30, 45}) {
                String utcLabel = FunctionCallingLoaderUtilTime.getZoneLabel("utc", hour, min);
                String gmtLabel = FunctionCallingLoaderUtilTime.getZoneLabel("gmt", hour, min);
                TimeZone tz = TimeZone.getTimeZone(String.format("GMT%+03d:%02d", hour, min));
                String time = FunctionCallingLoaderUtilTime.getFormattedTime(tz);
                response = response.replace(utcLabel, time);
                response = response.replace(gmtLabel, time);
            }
        }

        return response;
    }
}
