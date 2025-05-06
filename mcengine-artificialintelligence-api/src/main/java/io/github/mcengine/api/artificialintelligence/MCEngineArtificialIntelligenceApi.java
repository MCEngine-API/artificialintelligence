package io.github.mcengine.api.artificialintelligence;

import org.bukkit.plugin.Plugin;

import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import io.github.mcengine.api.artificialintelligence.Metrics;
import io.github.mcengine.api.artificialintelligence.model.*;
import io.github.mcengine.api.artificialintelligence.util.*;

/**
 * Main API class for MCEngineArtificialIntelligence.
 * Handles AI model initialization, extension loading (AddOns/DLCs), and token validation.
 */
public class MCEngineArtificialIntelligenceApi {

    /**
     * The Bukkit plugin instance associated with this AI API.
     */
    private final Plugin plugin;

    /**
     * Logger for logging plugin-related messages.
     */
    private final Logger logger;

    /**
     * The AI model instance for DeepSeek.
     */
    private final IMCEngineArtificialIntelligenceApiModel aiDeepSeek;

    /**
     * The AI model instance for OpenAI.
     */
    private final IMCEngineArtificialIntelligenceApiModel aiOpenAi;

    /**
     * The AI model instance for OpenRouter.
     */
    private final IMCEngineArtificialIntelligenceApiModel aiOpenRouter;

    /**
     * The AI model instance for a custom URL endpoint.
     */
    private final IMCEngineArtificialIntelligenceApiModel aiCustomUrl;

    // 🔥 GLOBAL cache shared across ALL plugins (platform → (model → instance))
    private static final Map<String, Map<String, IMCEngineArtificialIntelligenceApiModel>> modelCache = new HashMap<>();

    /**
     * Constructs a new AI API instance with the given plugin.
     * Initializes the AI model and loads addons and DLCs.
     *
     * @param plugin The Bukkit plugin instance.
     */
    public MCEngineArtificialIntelligenceApi(Plugin plugin) {
        new Metrics(plugin, 25556);
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        loadAddOns();
        loadDLCs();

        // Load AI model
        this.aiDeepSeek = new MCEngineArtificialIntelligenceApiModelDeepSeek(plugin);
        this.aiOpenAi = new MCEngineArtificialIntelligenceApiModelOpenAi(plugin);
        this.aiOpenRouter = new MCEngineArtificialIntelligenceApiModelOpenRouter(plugin);
        this.aiCustomUrl = new MCEngineArtificialIntelligenceApiModelCustomUrl(plugin);
    }

    public void checkUpdate(String gitPlatform, String org, String repository, String token) {
        MCEngineArtificialIntelligenceApiUtilUpdate.checkUpdate(plugin, gitPlatform, org, repository, token);
    }

    /**
     * Gets the associated plugin instance.
     *
     * @return The plugin instance.
     */
    public Plugin getPlugin() {
        return plugin;
    }

    /**
     * Loads AI AddOns from the "addons" folder.
     */
    private void loadAddOns() {
        MCEngineArtificialIntelligenceApiUtilExtension.loadExtensions(plugin, "addons", "AddOn");
    }

    /**
     * Loads AI DLCs from the "dlcs" folder.
     */
    private void loadDLCs() {
        MCEngineArtificialIntelligenceApiUtilExtension.loadExtensions(plugin, "dlcs", "DLC");
    }

    /**
     * Parses an expiration date from a supported input.
     *
     * @param input The date input (String or Date).
     * @return The parsed Date object.
     * @throws ParseException If the input string cannot be parsed.
     * @throws IllegalArgumentException If the input type is unsupported.
     */
    private static Date parseExpirationDate(Object input) throws ParseException {
        return MCEngineArtificialIntelligenceApiUtilToken.parseExpirationDate(input);
    }

    /**
     * Validates a token against the plugin name, secret key, and current date.
     *
     * @param pluginName The plugin name.
     * @param secretKey The secret key used for validation.
     * @param token The token string to validate.
     * @param nowDateInput The current date or date string.
     * @return True if the token is valid and not expired; false otherwise.
     */
    public static boolean validateToken(String pluginName, String secretKey, String token, Object nowDateInput) {
        return MCEngineArtificialIntelligenceApiUtilToken.validateToken(pluginName, secretKey, token, nowDateInput);
    }

    /**
     * Extracts the expiration date from a token.
     *
     * @param pluginName The plugin name.
     * @param secretKey The secret key used for validation.
     * @param token The token string.
     * @return The extracted expiration date, or a date representing epoch time (0) if invalid.
     */
    public static Date extractExpirationDate(String pluginName, String secretKey, String token) {
        return MCEngineArtificialIntelligenceApiUtilToken.extractExpirationDate(pluginName, secretKey, token);
    }

    /**
     * Registers an AI model instance if not already cached.
     * Called once per platform/model to initialize.
     *
     * @param platform AI platform
     * @param model    model name
     */
    public void registerModel(String platform, String model) {
        MCEngineArtificialIntelligenceApiUtilAi.registerModel(plugin, platform, model);
    }

    /**
     * Retrieves the registered AI model instance.
     *
     * @param platform AI platform
     * @param model    model name
     * @return AI model instance
     */
    public IMCEngineArtificialIntelligenceApiModel getAi(String platform, String model) {
        return MCEngineArtificialIntelligenceApiUtilAi.getAi(platform, model);
    }

    /**
     * Shortcut to get response from AI.
     *
     * @param platform AI platform
     * @param model    model name
     * @param message  prompt message
     * @return response from AI
     */
    public String getResponse(String platform, String model, String message) {
        return getAi(platform, model).getResponse(message);
    }

    /**
     * Sends a message to the DeepSeek AI model and returns its response.
     *
     * @param message The message or prompt to send to DeepSeek.
     * @return The response from DeepSeek.
     */
    public String getResponseFromDeepSeek(String message) {
        return aiDeepSeek.getResponse(message);
    }

    /**
     * Sends a message to the OpenAI AI model and returns its response.
     *
     * @param message The message or prompt to send to OpenAI.
     * @return The response from OpenAI.
     */
    public String getResponseFromOpenAi(String message) {
        return aiOpenAi.getResponse(message);
    }

    /**
     * Sends a message to the OpenRouter AI model and returns its response.
     *
     * @param message The message or prompt to send to OpenRouter.
     * @return The response from OpenRouter.
     */
    public String getResponseFromOpenRouter(String message) {
        return aiOpenRouter.getResponse(message);
    }

    /**
     * Sends a message to the Custom URL AI model and returns its response.
     *
     * @param message The message or prompt to send to the custom URL AI.
     * @return The response from the custom URL AI.
     */
    public String getResponseFromCustomUrl(String message) {
        return aiCustomUrl.getResponse(message);
    }
}
