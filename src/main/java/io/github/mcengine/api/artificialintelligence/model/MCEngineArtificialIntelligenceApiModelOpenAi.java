package io.github.mcengine.api.artificialintelligence.model;

import com.google.gson.JsonObject;
import io.github.mcengine.api.artificialintelligence.util.MCEngineArtificialIntelligenceApiUtilAi;
import org.bukkit.plugin.Plugin;

/**
 * OpenAI API implementation of {@link IMCEngineArtificialIntelligenceApiModel}.
 * Communicates with the OpenAI Chat API using the configured model and token.
 */
public class MCEngineArtificialIntelligenceApiModelOpenAi implements IMCEngineArtificialIntelligenceApiModel {

    /** The Bukkit plugin instance used for configuration access and logging. */
    private final Plugin plugin;

    /** The default token from config used to authenticate OpenAI API calls. */
    private final String defaultToken;

    /** The model name (e.g., "gpt-4", "gpt-3.5-turbo") to use for completion requests. */
    private final String aiModel;

    /**
     * Constructs an OpenAI API model integration using plugin config for credentials and endpoint setup.
     *
     * @param plugin The Bukkit plugin instance.
     * @param model  The name of the OpenAI model to use for responses.
     */
    public MCEngineArtificialIntelligenceApiModelOpenAi(Plugin plugin, String model) {
        this.plugin = plugin;
        this.defaultToken = plugin.getConfig().getString("ai.openai.token", null);
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
                "https://api.openai.com/v1/chat/completions",
                aiModel,
                defaultToken,
                token,
                systemPrompt,
                message,
                false
        );
    }
}
