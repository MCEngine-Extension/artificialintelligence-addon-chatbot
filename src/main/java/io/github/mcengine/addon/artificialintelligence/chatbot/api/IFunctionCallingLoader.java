package io.github.mcengine.addon.artificialintelligence.chatbot.api;

import java.util.List;

public interface IFunctionCallingLoader {
    List<FunctionRule> loadFunctionRules();
}
