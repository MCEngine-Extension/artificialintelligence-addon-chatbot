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
            // Entity
            Map.of("match", Arrays.asList("What mobs are near me?", "List nearby entities"), "response", "Nearby entities:\n{nearby_entities_count}"),
            Map.of("match", Arrays.asList("How many allays nearby?", "Nearby allay count"), "response", "There are {nearby_allay_count} allays near you."),
            Map.of("match", Arrays.asList("How many armadillos nearby?", "Nearby armadillo count"), "response", "There are {nearby_armadillo_count} armadillos near you."),
            Map.of("match", Arrays.asList("How many axolotls nearby?", "Nearby axolotl count"), "response", "There are {nearby_axolotl_count} axolotls near you."),
            Map.of("match", Arrays.asList("How many bats nearby?", "Nearby bat count"), "response", "There are {nearby_bat_count} bats near you."),
            Map.of("match", Arrays.asList("How many bees nearby?", "Nearby bee count"), "response", "There are {nearby_bee_count} bees near you."),
            Map.of("match", Arrays.asList("How many blazes nearby?", "Nearby blaze count"), "response", "There are {nearby_blaze_count} blazes near you."),
            Map.of("match", Arrays.asList("How many camels nearby?", "Nearby camel count"), "response", "There are {nearby_camel_count} camels near you."),
            Map.of("match", Arrays.asList("How many cats nearby?", "Nearby cat count"), "response", "There are {nearby_cat_count} cats near you."),
            Map.of("match", Arrays.asList("How many cave spiders nearby?", "Nearby cave spider count"), "response", "There are {nearby_cave_spider_count} cave spiders near you."),
            Map.of("match", Arrays.asList("How many chickens nearby?", "Nearby chicken count"), "response", "There are {nearby_chicken_count} chickens near you."),
            Map.of("match", Arrays.asList("How many cod nearby?", "Nearby cod count"), "response", "There are {nearby_cod_count} cod near you."),
            Map.of("match", Arrays.asList("How many cows nearby?", "Nearby cow count"), "response", "There are {nearby_cow_count} cows near you."),
            Map.of("match", Arrays.asList("How many creepers nearby?", "Nearby creeper count"), "response", "There are {nearby_creeper_count} creepers near you."),
            Map.of("match", Arrays.asList("How many dolphins nearby?", "Nearby dolphin count"), "response", "There are {nearby_dolphin_count} dolphins near you."),
            Map.of("match", Arrays.asList("How many donkeys nearby?", "Nearby donkey count"), "response", "There are {nearby_donkey_count} donkeys near you."),
            Map.of("match", Arrays.asList("How many drowned nearby?", "Nearby drowned count"), "response", "There are {nearby_drowned_count} drowned near you."),
            Map.of("match", Arrays.asList("How many elder guardians nearby?", "Nearby elder guardian count"), "response", "There are {nearby_elder_guardian_count} elder guardians near you."),
            Map.of("match", Arrays.asList("How many ender dragons nearby?", "Nearby ender dragon count"), "response", "There are {nearby_ender_dragon_count} ender dragons near you."),
            Map.of("match", Arrays.asList("How many endermites nearby?", "Nearby endermite count"), "response", "There are {nearby_endermite_count} endermites near you."),
            Map.of("match", Arrays.asList("How many evokers nearby?", "Nearby evoker count"), "response", "There are {nearby_evoker_count} evokers near you."),
            Map.of("match", Arrays.asList("How many foxes nearby?", "Nearby fox count"), "response", "There are {nearby_fox_count} foxes near you."),
            Map.of("match", Arrays.asList("How many frogs nearby?", "Nearby frog count"), "response", "There are {nearby_frog_count} frogs near you."),
            Map.of("match", Arrays.asList("How many ghasts nearby?", "Nearby ghast count"), "response", "There are {nearby_ghast_count} ghasts near you."),
            Map.of("match", Arrays.asList("How many glow squids nearby?", "Nearby glow squid count"), "response", "There are {nearby_glow_squid_count} glow squids near you."),
            Map.of("match", Arrays.asList("How many goats nearby?", "Nearby goat count"), "response", "There are {nearby_goat_count} goats near you."),
            Map.of("match", Arrays.asList("How many guardians nearby?", "Nearby guardian count"), "response", "There are {nearby_guardian_count} guardians near you."),
            Map.of("match", Arrays.asList("How many hoglins nearby?", "Nearby hoglin count"), "response", "There are {nearby_hoglin_count} hoglins near you."),
            Map.of("match", Arrays.asList("How many horses nearby?", "Nearby horse count"), "response", "There are {nearby_horse_count} horses near you."),
            Map.of("match", Arrays.asList("How many husks nearby?", "Nearby husk count"), "response", "There are {nearby_husk_count} husks near you."),
            Map.of("match", Arrays.asList("How many illusioners nearby?", "Nearby illusioner count"), "response", "There are {nearby_illusioner_count} illusioners near you."),
            Map.of("match", Arrays.asList("How many iron golems nearby?", "Nearby iron golem count"), "response", "There are {nearby_iron_golem_count} iron golems near you."),
            Map.of("match", Arrays.asList("How many llamas nearby?", "Nearby llama count"), "response", "There are {nearby_llama_count} llamas near you."),
            Map.of("match", Arrays.asList("How many magma cubes nearby?", "Nearby magma cube count"), "response", "There are {nearby_magma_cube_count} magma cubes near you."),
            Map.of("match", Arrays.asList("How many mooshrooms nearby?", "Nearby mooshroom count"), "response", "There are {nearby_mooshroom_count} mooshrooms near you."),
            Map.of("match", Arrays.asList("How many mules nearby?", "Nearby mule count"), "response", "There are {nearby_mule_count} mules near you."),
            Map.of("match", Arrays.asList("How many ocelots nearby?", "Nearby ocelot count"), "response", "There are {nearby_ocelot_count} ocelots near you."),
            Map.of("match", Arrays.asList("How many pandas nearby?", "Nearby panda count"), "response", "There are {nearby_panda_count} pandas near you."),
            Map.of("match", Arrays.asList("How many parrots nearby?", "Nearby parrot count"), "response", "There are {nearby_parrot_count} parrots near you."),
            Map.of("match", Arrays.asList("How many phantoms nearby?", "Nearby phantom count"), "response", "There are {nearby_phantom_count} phantoms near you."),
            Map.of("match", Arrays.asList("How many pigs nearby?", "Nearby pig count"), "response", "There are {nearby_pig_count} pigs near you."),
            Map.of("match", Arrays.asList("How many piglins nearby?", "Nearby piglin count"), "response", "There are {nearby_piglin_count} piglins near you."),
            Map.of("match", Arrays.asList("How many piglin brutes nearby?", "Nearby piglin brute count"), "response", "There are {nearby_piglin_brute_count} piglin brutes near you."),
            Map.of("match", Arrays.asList("How many pillagers nearby?", "Nearby pillager count"), "response", "There are {nearby_pillager_count} pillagers near you."),
            Map.of("match", Arrays.asList("How many polar bears nearby?", "Nearby polar_bear count"), "response", "There are {nearby_polar_bear_count} polar bears near you."),
            Map.of("match", Arrays.asList("How many pufferfish nearby?", "Nearby pufferfish count"), "response", "There are {nearby_pufferfish_count} pufferfish near you."),
            Map.of("match", Arrays.asList("How many rabbits nearby?", "Nearby rabbit count"), "response", "There are {nearby_rabbit_count} rabbits near you."),
            Map.of("match", Arrays.asList("How many ravagers nearby?", "Nearby ravager count"), "response", "There are {nearby_ravager_count} ravagers near you."),
            Map.of("match", Arrays.asList("How many salmon nearby?", "Nearby salmon count"), "response", "There are {nearby_salmon_count} salmon near you."),
            Map.of("match", Arrays.asList("How many sheep nearby?", "Nearby sheep count"), "response", "There are {nearby_sheep_count} sheep near you."),
            Map.of("match", Arrays.asList("How many shulkers nearby?", "Nearby shulker count"), "response", "There are {nearby_shulker_count} shulkers near you."),
            Map.of("match", Arrays.asList("How many silverfish nearby?", "Nearby silverfish count"), "response", "There are {nearby_silverfish_count} silverfish near you."),
            Map.of("match", Arrays.asList("How many skeletons nearby?", "Nearby skeleton count"), "response", "There are {nearby_skeleton_count} skeletons near you."),
            Map.of("match", Arrays.asList("How many skeleton horses nearby?", "Nearby skeleton_horse count"), "response", "There are {nearby_skeleton_horse_count} skeleton horses near you."),
            Map.of("match", Arrays.asList("How many slimes nearby?", "Nearby slime count"), "response", "There are {nearby_slime_count} slimes near you."),
            Map.of("match", Arrays.asList("How many sniffers nearby?", "Nearby sniffer count"), "response", "There are {nearby_sniffer_count} sniffers near you."),
            Map.of("match", Arrays.asList("How many snow golems nearby?", "Nearby snow_golem count"), "response", "There are {nearby_snow_golem_count} snow golems near you."),
            Map.of("match", Arrays.asList("How many spiders nearby?", "Nearby spider count"), "response", "There are {nearby_spider_count} spiders near you."),
            Map.of("match", Arrays.asList("How many squids nearby?", "Nearby squid count"), "response", "There are {nearby_squid_count} squids near you."),
            Map.of("match", Arrays.asList("How many strays nearby?", "Nearby stray count"), "response", "There are {nearby_stray_count} strays near you."),
            Map.of("match", Arrays.asList("How many striders nearby?", "Nearby strider count"), "response", "There are {nearby_strider_count} striders near you."),
            Map.of("match", Arrays.asList("How many trader llamas nearby?", "Nearby trader_llama count"), "response", "There are {nearby_trader_llama_count} trader llamas near you."),
            Map.of("match", Arrays.asList("How many tropical fish nearby?", "Nearby tropical_fish count"), "response", "There are {nearby_tropical_fish_count} tropical fish near you."),
            Map.of("match", Arrays.asList("How many turtles nearby?", "Nearby turtle count"), "response", "There are {nearby_turtle_count} turtles near you."),
            Map.of("match", Arrays.asList("How many vexes nearby?", "Nearby vex count"), "response", "There are {nearby_vex_count} vexes near you."),
            Map.of("match", Arrays.asList("How many vindicators nearby?", "Nearby vindicator count"), "response", "There are {nearby_vindicator_count} vindicators near you."),
            Map.of("match", Arrays.asList("How many wardens nearby?", "Nearby warden count"), "response", "There are {nearby_warden_count} wardens near you."),
            Map.of("match", Arrays.asList("How many witches nearby?", "Nearby witch count"), "response", "There are {nearby_witch_count} witches near you."),
            Map.of("match", Arrays.asList("How many withers nearby?", "Nearby wither count"), "response", "There are {nearby_wither_count} withers near you."),
            Map.of("match", Arrays.asList("How many wither skeletons nearby?", "Nearby wither_skeleton count"), "response", "There are {nearby_wither_skeleton_count} wither skeletons near you."),
            Map.of("match", Arrays.asList("How many wolves nearby?", "Nearby wolf count"), "response", "There are {nearby_wolf_count} wolves near you."),
            Map.of("match", Arrays.asList("How many zoglins nearby?", "Nearby zoglin count"), "response", "There are {nearby_zoglin_count} zoglins near you."),
            Map.of("match", Arrays.asList("How many zombie horses nearby?", "Nearby zombie_horse count"), "response", "There are {nearby_zombie_horse_count} zombie horses near you."),
            Map.of("match", Arrays.asList("How many zombie villagers nearby?", "Nearby zombie_villager count"), "response", "There are {nearby_zombie_villager_count} zombie villagers near you."),
            Map.of("match", Arrays.asList("How many zombified piglins nearby?", "Nearby zombified_piglin count"), "response", "There are {nearby_zombified_piglin_count} zombified piglins near you."),
            Map.of("match", Arrays.asList("How many zombies nearby?", "Nearby zombie count"), "response", "There are {nearby_zombie_count} zombies near you."),

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
