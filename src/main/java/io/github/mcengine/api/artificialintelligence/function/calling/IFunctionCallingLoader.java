package io.github.mcengine.api.artificialintelligence.function.calling;

import java.util.List;

/**
 * Interface for loading function calling rules used by the chatbot.
 * Implementations of this interface provide custom logic for sourcing {@link FunctionRule} definitions,
 * such as from JSON files, databases, or other storage mechanisms.
 */
public interface IFunctionCallingLoader {

    /**
     * Loads all function calling rules from the underlying data source.
     *
     * @return A list of {@link FunctionRule} objects loaded from the data source.
     */
    List<FunctionRule> loadFunctionRules();
}
