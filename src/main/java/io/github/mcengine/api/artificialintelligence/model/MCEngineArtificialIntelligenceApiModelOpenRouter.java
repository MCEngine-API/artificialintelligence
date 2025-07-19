package io.github.mcengine.api.artificialintelligence.model;

import com.google.gson.JsonObject;
import io.github.mcengine.api.artificialintelligence.util.MCEngineArtificialIntelligenceApiUtilAi;
import org.bukkit.plugin.Plugin;

/**
 * OpenRouter implementation of {@link IMCEngineArtificialIntelligenceApiModel}.
 * This class communicates with the OpenRouter API to fetch AI-generated responses.
 */
public class MCEngineArtificialIntelligenceApiModelOpenRouter implements IMCEngineArtificialIntelligenceApiModel {

    /** The Bukkit plugin instance used to access configuration and log errors/warnings. */
    private final Plugin plugin;

    /** The default authentication token used when a user-specific token is not supplied. */
    private final String defaultToken;

    /** The AI model name (e.g., "openrouter/gpt-4") to use for API calls. */
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

    @Override
    public JsonObject getResponse(String systemPrompt, String message) {
        return getResponse(defaultToken, systemPrompt, message);
    }

    @Override
    public JsonObject getResponse(String token, String systemPrompt, String message) {
        return MCEngineArtificialIntelligenceApiUtilAi.getResponse(
                plugin,
                "https://openrouter.ai/api/v1/chat/completions",
                aiModel,
                defaultToken,
                token,
                systemPrompt,
                message,
                true
        );
    }
}
