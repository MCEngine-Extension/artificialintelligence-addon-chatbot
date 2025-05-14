package io.github.mcengine.addon.artificialintelligence.chatbot.api.functions.calling;

import io.github.mcengine.addon.artificialintelligence.chatbot.api.functions.calling.FunctionRule;
import java.util.List;

public interface IFunctionCallingLoader {
    List<FunctionRule> loadFunctionRules();
}
