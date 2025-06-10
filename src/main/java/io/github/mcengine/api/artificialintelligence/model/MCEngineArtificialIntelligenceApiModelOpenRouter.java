package io.github.mcengine.api.artificialintelligence.model;

import org.bukkit.plugin.Plugin;
import io.github.mcengine.api.artificialintelligence.util.MCEngineArtificialIntelligenceApiUtilAi;

/**
 * OpenRouter implementation of {@link IMCEngineArtificialIntelligenceApiModel}.
 * This class communicates with the OpenRouter API to fetch AI-generated responses.
 */
public class MCEngineArtificialIntelligenceApiModelOpenRouter implements IMCEngineArtificialIntelligenceApiModel {

    /**
     * The Bukkit plugin instance used to access configuration and log errors/warnings.
     */
    private final Plugin plugin;

    /**
     * The default authentication token used when a user-specific token is not supplied.
     * Retrieved from the config path {@code ai.openrouter.token}.
     */
    private final String defaultToken;

    /**
     * The AI model name (e.g., "openrouter/gpt-4") to use for API calls.
     */
    private final String aiModel;

    /**
     * Constructs a new OpenRouter AI model handler using a specified model.
     *
     * @param plugin The Bukkit plugin instance to retrieve configuration and logger.
     * @param model  The AI model name to use.
     */
    public MCEngineArtificialIntelligenceApiModelOpenRouter(Plugin plugin, String model) {
        this.plugin = plugin;
        this.defaultToken = plugin.getConfig().getString("ai.openrouter.token", null);
        this.aiModel = model;
    }

    /**
     * Sends a user message to the OpenRouter API using the default token from configuration.
     *
     * @param message The user input message to send.
     * @return The AI-generated response string.
     */
    @Override
    public String getResponse(String message) {
        return getResponse(defaultToken, message);
    }

    /**
     * Sends a user message to the OpenRouter API using a provided user-specific token.
     *
     * @param token   A user-specific authentication token to use for the API call.
     * @param message The user input message to send to the AI.
     * @return The AI-generated response string, or an error message if the request fails.
     */
    @Override
    public String getResponse(String token, String message) {
        return MCEngineArtificialIntelligenceApiUtilAi.getResponse(
                plugin,
                "https://openrouter.ai/api/v1/chat/completions",
                aiModel,
                defaultToken,
                token,
                message,
                true
        );
    }
}
