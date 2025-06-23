package io.github.mcengine.extension.addon.artificialintelligence.chatbot.tabcompleter;

import io.github.mcengine.api.artificialintelligence.util.MCEngineArtificialIntelligenceApiUtilAi;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.*;

/**
 * Tab completer for the /chatbot command.
 * <p>
 * Supports suggestions for:
 * - /chatbot {platform} {model}
 * - /chatbot set email {your@email.com}
 */
public class ChatBotTabCompleter implements TabCompleter {

    /**
     * Provides tab completion suggestions for the /chatbot command.
     *
     * @param sender  The sender of the command.
     * @param command The command object.
     * @param label   The command label.
     * @param args    The arguments passed to the command.
     * @return A list of suggestions based on current input.
     */
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        Map<String, Map<String, ?>> models = MCEngineArtificialIntelligenceApiUtilAi.getAllModels();

        // /chatbot <first-arg>
        if (args.length == 1) {
            List<String> suggestions = new ArrayList<>(models.keySet());
            suggestions.add("set");
            Collections.sort(suggestions);
            return filterPrefix(suggestions, args[0]);
        }

        // /chatbot set <second-arg>
        if (args.length == 2 && args[0].equalsIgnoreCase("set")) {
            return filterPrefix(List.of("email"), args[1]);
        }

        // /chatbot set email <email-text> → no tab suggestion
        if (args.length == 3 && args[0].equalsIgnoreCase("set") && args[1].equalsIgnoreCase("email")) {
            return Collections.emptyList();
        }

        // /chatbot <platform> <model>
        if (args.length == 2) {
            String platform = args[0];
            if (!models.containsKey(platform)) return Collections.emptyList();

            List<String> modelNames = new ArrayList<>(models.get(platform).keySet());
            Collections.sort(modelNames);
            return filterPrefix(modelNames, args[1]);
        }

        return Collections.emptyList();
    }

    /**
     * Filters a list of options to match the input prefix.
     *
     * @param options The list of possible values.
     * @param prefix  The input to match.
     * @return A filtered list of suggestions.
     */
    private List<String> filterPrefix(List<String> options, String prefix) {
        List<String> result = new ArrayList<>();
        for (String option : options) {
            if (option.toLowerCase().startsWith(prefix.toLowerCase())) {
                result.add(option);
            }
        }
        return result;
    }
}
