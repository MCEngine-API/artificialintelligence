package io.github.mcengine.api.artificialintelligence.model;

import com.google.gson.JsonObject;
import io.github.mcengine.api.artificialintelligence.util.MCEngineArtificialIntelligenceApiUtilAi;
import org.bukkit.plugin.Plugin;

/**
 * Custom URL AI implementation of {@link IMCEngineArtificialIntelligenceApiModel}.
 * Communicates with a user-defined API endpoint specified in the plugin configuration.
 */
public class MCEngineArtificialIntelligenceApiModelCustomUrl implements IMCEngineArtificialIntelligenceApiModel {

    /**
     * The Bukkit plugin instance used for accessing configuration and logging.
     */
    private final Plugin plugin;

    /**
     * The identifier for the custom AI server (used to fetch config values like token and endpoint).
     */
    private final String serverName;

    /**
     * The default authentication token defined in the config for this server.
     * Used if a user-specific token is not provided.
     */
    private final String defaultToken;

    /**
     * The full URL of the custom API endpoint for this AI model.
     */
    private final String endpoint;

    /**
     * The name of the AI model to be used in API requests.
     */
    private final String aiModel;

    /**
     * Constructs a new Custom URL AI model handler for a specific server and model.
     * <p>
     * This constructor retrieves the API endpoint URL, token, and default model
     * from the plugin configuration path {@code ai.custom.{server}}.
     * It allows multiple servers, each with their own endpoint and token, to be used concurrently.
     *
     * @param plugin The Bukkit plugin instance for configuration and logging.
     * @param server The server identifier (corresponding to the config section {@code ai.custom.{server}}).
     * @param model  The AI model name; if null, uses the model from config {@code ai.custom.{server}.model}.
     */
    public MCEngineArtificialIntelligenceApiModelCustomUrl(Plugin plugin, String server, String model) {
        this.plugin = plugin;
        this.serverName = server;
        String configBase = "ai.custom." + server + ".";
        this.defaultToken = plugin.getConfig().getString(configBase + "token", null);
        this.endpoint = plugin.getConfig().getString(configBase + "url", "http://localhost:11434/v1/chat/completions");
        this.aiModel = model;
    }

    /**
     * Sends a user message to the custom API endpoint using the default token from config
     * and returns the full JSON response.
     *
     * @param message The user input message to send.
     * @return The full JSON response from the custom AI endpoint.
     */
    @Override
    public JsonObject getResponse(String message) {
        return getResponse(defaultToken, message);
    }

    /**
     * Sends a user message to the custom API endpoint using the provided user-specific token
     * and returns the full JSON response.
     *
     * @param token   The user-specific token used for authentication in the API request.
     * @param message The user input message to send.
     * @return The full JSON response from the custom AI endpoint.
     */
    @Override
    public JsonObject getResponse(String token, String message) {
        return MCEngineArtificialIntelligenceApiUtilAi.getResponse(
                plugin,
                endpoint,
                aiModel,
                defaultToken,
                token,
                message,
                false
        );
    }
}
