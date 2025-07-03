package io.github.mcengine.api.artificialintelligence.model;

import com.google.gson.JsonObject;
import io.github.mcengine.api.artificialintelligence.util.MCEngineArtificialIntelligenceApiUtilAi;
import org.bukkit.plugin.Plugin;

/**
 * DeepSeek implementation of {@link IMCEngineArtificialIntelligenceApiModel}.
 * This class communicates with the DeepSeek API to fetch AI-generated responses based on user prompts.
 */
public class MCEngineArtificialIntelligenceApiModelDeepSeek implements IMCEngineArtificialIntelligenceApiModel {

    /**
     * The Bukkit plugin instance used for accessing configuration and logging.
     */
    private final Plugin plugin;

    /**
     * The default token configured for accessing the DeepSeek API.
     * Used when a user-specific token is not provided.
     */
    private final String defaultToken;

    /**
     * The name of the AI model to be used when sending requests to DeepSeek.
     */
    private final String aiModel;

    /**
     * Constructs a DeepSeek model handler instance using configuration and model name.
     *
     * @param plugin The Bukkit plugin instance for accessing configuration and logging.
     * @param model  The name of the AI model to use (e.g., "deepseek-chat").
     */
    public MCEngineArtificialIntelligenceApiModelDeepSeek(Plugin plugin, String model) {
        this.plugin = plugin;
        this.defaultToken = plugin.getConfig().getString("ai.deepseek.token", null);
        this.aiModel = model;
    }

    /**
     * Sends a message to the DeepSeek API using the default token from the configuration.
     *
     * @param message The user input message or prompt to send.
     * @return The full JSON response from the DeepSeek API.
     */
    @Override
    public JsonObject getResponse(String message) {
        return getResponse(defaultToken, message);
    }

    /**
     * Sends a message to the DeepSeek API using the provided user-specific token.
     *
     * @param token   The user-specific authentication token for DeepSeek.
     * @param message The user input message or prompt to send.
     * @return A {@link JsonObject} containing the full response from DeepSeek API.
     */
    @Override
    public JsonObject getResponse(String token, String message) {
        return MCEngineArtificialIntelligenceApiUtilAi.getResponse(
                plugin,
                "https://api.deepseek.com/v1/chat/completions",
                aiModel,
                defaultToken,
                token,
                message,
                false
        );
    }
}
