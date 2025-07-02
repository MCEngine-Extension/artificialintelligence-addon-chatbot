package io.github.mcengine.extension.addon.artificialintelligence.chatbot.api;

import io.github.mcengine.api.core.extension.addon.MCEngineAddOnLogger;
import io.github.mcengine.extension.addon.artificialintelligence.chatbot.api.json.FunctionCallingJson;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.*;
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

    private final Plugin plugin;

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
     *
     * @param response The raw response containing placeholders.
     * @param player   The player whose data is used for substitution.
     * @return A fully resolved string with all placeholders replaced.
     */
    private String applyPlaceholders(String response, Player player) {
        World world = player.getWorld();

        response = response
                // Nearby entity count
                .replace("{nearby_entities_count}", getNearbyEntities(plugin, player, 20))
                .replace("{nearby_allay_count}", getNearbyEntities(plugin, player, "allay", 20))
                .replace("{nearby_armadillo_count}", getNearbyEntities(plugin, player, "armadillo", 20))
                .replace("{nearby_axolotl_count}", getNearbyEntities(plugin, player, "axolotl", 20))
                .replace("{nearby_bat_count}", getNearbyEntities(plugin, player, "bat", 20))
                .replace("{nearby_bee_count}", getNearbyEntities(plugin, player, "bee", 20))
                .replace("{nearby_blaze_count}", getNearbyEntities(plugin, player, "blaze", 20))
                .replace("{nearby_bogged_count}", getNearbyEntities(plugin, player, "bogged", 20))
                .replace("{nearby_breeze_count}", getNearbyEntities(plugin, player, "breeze", 20))
                .replace("{nearby_camel_count}", getNearbyEntities(plugin, player, "camel", 20))
                .replace("{nearby_cat_count}", getNearbyEntities(plugin, player, "cat", 20))
                .replace("{nearby_cave_spider_count}", getNearbyEntities(plugin, player, "cave_spider", 20))
                .replace("{nearby_chicken_count}", getNearbyEntities(plugin, player, "chicken", 20))
                .replace("{nearby_cod_count}", getNearbyEntities(plugin, player, "cod", 20))
                .replace("{nearby_cow_count}", getNearbyEntities(plugin, player, "cow", 20))
                .replace("{nearby_creeper_count}", getNearbyEntities(plugin, player, "creeper", 20))
                .replace("{nearby_dolphin_count}", getNearbyEntities(plugin, player, "dolphin", 20))
                .replace("{nearby_donkey_count}", getNearbyEntities(plugin, player, "donkey", 20))
                .replace("{nearby_drowned_count}", getNearbyEntities(plugin, player, "drowned", 20))
                .replace("{nearby_elder_guardian_count}", getNearbyEntities(plugin, player, "elder_guardian", 20))
                .replace("{nearby_ender_dragon_count}", getNearbyEntities(plugin, player, "ender_dragon", 20))
                .replace("{nearby_endermite_count}", getNearbyEntities(plugin, player, "endermite", 20))
                .replace("{nearby_evoker_count}", getNearbyEntities(plugin, player, "evoker", 20))
                .replace("{nearby_fox_count}", getNearbyEntities(plugin, player, "fox", 20))
                .replace("{nearby_frog_count}", getNearbyEntities(plugin, player, "frog", 20))
                .replace("{nearby_ghast_count}", getNearbyEntities(plugin, player, "ghast", 20))
                .replace("{nearby_glow_squid_count}", getNearbyEntities(plugin, player, "glow_squid", 20))
                .replace("{nearby_goat_count}", getNearbyEntities(plugin, player, "goat", 20))
                .replace("{nearby_guardian_count}", getNearbyEntities(plugin, player, "guardian", 20))
                .replace("{nearby_hoglin_count}", getNearbyEntities(plugin, player, "hoglin", 20))
                .replace("{nearby_horse_count}", getNearbyEntities(plugin, player, "horse", 20))
                .replace("{nearby_husk_count}", getNearbyEntities(plugin, player, "husk", 20))
                .replace("{nearby_illusioner_count}", getNearbyEntities(plugin, player, "illusioner", 20))
                .replace("{nearby_iron_golem_count}", getNearbyEntities(plugin, player, "iron_golem", 20))
                .replace("{nearby_llama_count}", getNearbyEntities(plugin, player, "llama", 20))
                .replace("{nearby_magma_cube_count}", getNearbyEntities(plugin, player, "magma_cube", 20))
                .replace("{nearby_mooshroom_count}", getNearbyEntities(plugin, player, "mooshroom", 20))
                .replace("{nearby_mule_count}", getNearbyEntities(plugin, player, "mule", 20))
                .replace("{nearby_ocelot_count}", getNearbyEntities(plugin, player, "ocelot", 20))
                .replace("{nearby_panda_count}", getNearbyEntities(plugin, player, "panda", 20))
                .replace("{nearby_parrot_count}", getNearbyEntities(plugin, player, "parrot", 20))
                .replace("{nearby_phantom_count}", getNearbyEntities(plugin, player, "phantom", 20))
                .replace("{nearby_pig_count}", getNearbyEntities(plugin, player, "pig", 20))
                .replace("{nearby_piglin_count}", getNearbyEntities(plugin, player, "piglin", 20))
                .replace("{nearby_piglin_brute_count}", getNearbyEntities(plugin, player, "piglin_brute", 20))
                .replace("{nearby_pillager_count}", getNearbyEntities(plugin, player, "pillager", 20))
                .replace("{nearby_polar_bear_count}", getNearbyEntities(plugin, player, "polar_bear", 20))
                .replace("{nearby_pufferfish_count}", getNearbyEntities(plugin, player, "pufferfish", 20))
                .replace("{nearby_rabbit_count}", getNearbyEntities(plugin, player, "rabbit", 20))
                .replace("{nearby_ravager_count}", getNearbyEntities(plugin, player, "ravager", 20))
                .replace("{nearby_salmon_count}", getNearbyEntities(plugin, player, "salmon", 20))
                .replace("{nearby_sheep_count}", getNearbyEntities(plugin, player, "sheep", 20))
                .replace("{nearby_shulker_count}", getNearbyEntities(plugin, player, "shulker", 20))
                .replace("{nearby_silverfish_count}", getNearbyEntities(plugin, player, "silverfish", 20))
                .replace("{nearby_skeleton_count}", getNearbyEntities(plugin, player, "skeleton", 20))
                .replace("{nearby_skeleton_horse_count}", getNearbyEntities(plugin, player, "skeleton_horse", 20))
                .replace("{nearby_slime_count}", getNearbyEntities(plugin, player, "slime", 20))
                .replace("{nearby_sniffer_count}", getNearbyEntities(plugin, player, "sniffer", 20))
                .replace("{nearby_snow_golem_count}", getNearbyEntities(plugin, player, "snow_golem", 20))
                .replace("{nearby_spider_count}", getNearbyEntities(plugin, player, "spider", 20))
                .replace("{nearby_squid_count}", getNearbyEntities(plugin, player, "squid", 20))
                .replace("{nearby_stray_count}", getNearbyEntities(plugin, player, "stray", 20))
                .replace("{nearby_strider_count}", getNearbyEntities(plugin, player, "strider", 20))
                .replace("{nearby_trader_llama_count}", getNearbyEntities(plugin, player, "trader_llama", 20))
                .replace("{nearby_tropical_fish_count}", getNearbyEntities(plugin, player, "tropical_fish", 20))
                .replace("{nearby_turtle_count}", getNearbyEntities(plugin, player, "turtle", 20))
                .replace("{nearby_vex_count}", getNearbyEntities(plugin, player, "vex", 20))
                .replace("{nearby_vindicator_count}", getNearbyEntities(plugin, player, "vindicator", 20))
                .replace("{nearby_warden_count}", getNearbyEntities(plugin, player, "warden", 20))
                .replace("{nearby_witch_count}", getNearbyEntities(plugin, player, "witch", 20))
                .replace("{nearby_wither_count}", getNearbyEntities(plugin, player, "wither", 20))
                .replace("{nearby_wither_skeleton_count}", getNearbyEntities(plugin, player, "wither_skeleton", 20))
                .replace("{nearby_wolf_count}", getNearbyEntities(plugin, player, "wolf", 20))
                .replace("{nearby_zoglin_count}", getNearbyEntities(plugin, player, "zoglin", 20))
                .replace("{nearby_zombie_count}", getNearbyEntities(plugin, player, "zombie", 20))
                .replace("{nearby_zombie_horse_count}", getNearbyEntities(plugin, player, "zombie_horse", 20))
                .replace("{nearby_zombie_villager_count}", getNearbyEntities(plugin, player, "zombie_villager", 20))
                .replace("{nearby_zombified_piglin_count}", getNearbyEntities(plugin, player, "zombified_piglin", 20))

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
                .replace("{world_entity_count}", getSafeEntityCount(plugin, world))
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
