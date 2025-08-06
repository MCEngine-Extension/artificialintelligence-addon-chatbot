package io.github.mcengine.extension.addon.artificialintelligence.chatbot.tabcompleter;

import io.github.mcengine.api.artificialintelligence.util.MCEngineArtificialIntelligenceApiUtilAi;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.*;

/**
 * Tab completer for /ai chatbot subcommand.
 * <p>
 * Supports suggestions for:
 * - /ai chatbot set email &lt;your@email.com&gt;
 * - /ai chatbot &lt;platform&gt; &lt;model&gt;
 */
public class ChatBotTabCompleter implements TabCompleter {

    /**
     * Provides suggestions for /ai chatbot subcommand.
     *
     * @param sender  The command sender.
     * @param command The command being run.
     * @param label   The command alias.
     * @param args    The arguments passed after "/ai chatbot"
     * @return A list of completions or null for none.
     */
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        Map<String, Map<String, ?>> models = MCEngineArtificialIntelligenceApiUtilAi.getAllModels();

        // /ai chatbot <first>
        if (args.length == 1) {
            List<String> suggestions = new ArrayList<>(models.keySet());
            suggestions.add("set");
            Collections.sort(suggestions);
            return filterPrefix(suggestions, args[0]);
        }

        // /ai chatbot set <second>
        if (args.length == 2 && args[0].equalsIgnoreCase("set")) {
            return filterPrefix(List.of("email"), args[1]);
        }

        // /ai chatbot set email <email> — no suggestions
        if (args.length == 3 && args[0].equalsIgnoreCase("set") && args[1].equalsIgnoreCase("email")) {
            return Collections.emptyList();
        }

        // /ai chatbot <platform> <model>
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
     * Filters tab completion results by prefix.
     *
     * @param options List of options to filter.
     * @param prefix  The input to filter by.
     * @return Filtered suggestions list.
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
