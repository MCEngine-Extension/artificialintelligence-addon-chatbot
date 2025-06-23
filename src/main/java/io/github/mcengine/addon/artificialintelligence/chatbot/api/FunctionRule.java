package io.github.mcengine.addon.artificialintelligence.chatbot.api;

import java.util.List;

/**
 * Represents a rule for matching player input to a static chatbot response.
 * Each rule contains a list of possible match strings and a single response.
 */
public class FunctionRule {

    /**
     * A list of input phrases or patterns that should trigger this rule.
     */
    public List<String> match;

    /**
     * The chatbot's response when this rule is triggered.
     */
    public String response;

    /**
     * Constructs a FunctionRule with the specified matching inputs and response text.
     *
     * @param match    A list of strings that represent valid triggers for this rule.
     * @param response The response string returned when this rule matches.
     */
    public FunctionRule(List<String> match, String response) {
        this.match = match;
        this.response = response;
    }
}
