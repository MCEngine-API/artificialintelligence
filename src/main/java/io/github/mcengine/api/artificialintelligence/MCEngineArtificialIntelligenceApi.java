package io.github.mcengine.api.artificialintelligence;

import com.google.gson.JsonObject;
import io.github.mcengine.api.artificialintelligence.database.IMCEngineArtificialIntelligenceDB;
import io.github.mcengine.api.artificialintelligence.function.calling.FunctionCallingLoader;
import io.github.mcengine.api.artificialintelligence.model.IMCEngineArtificialIntelligenceApiModel;
import io.github.mcengine.api.artificialintelligence.util.MCEngineArtificialIntelligenceApiUtilAi;
import io.github.mcengine.api.artificialintelligence.util.MCEngineArtificialIntelligenceApiUtilBotManager;
import io.github.mcengine.api.artificialintelligence.util.MCEngineArtificialIntelligenceApiUtilBotTask;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Main API class for MCEngineArtificialIntelligence.
 * Handles AI model initialization, response handling, token usage, and task management.
 */
public class MCEngineArtificialIntelligenceApi {

    /**
     * The FunctionCallingLoader instance for chatbot rule-based matching.
     */
    private FunctionCallingLoader functionCallingLoader;

    /**
     * The logger used for diagnostic output.
     */
    private Logger logger;

    /**
     * Initializes the FunctionCallingLoader for rule matching.
     *
     * @param plugin The plugin instance.
     * @param folderPath Path to rule directory relative to plugin's data folder.
     * @param logger The logger used for messages.
     */
    public void initializeFunctionCallingLoader(Plugin plugin, String folderPath, Logger logger) {
        this.logger = logger;
        this.functionCallingLoader = new FunctionCallingLoader(plugin, folderPath, logger);
    }

    /**
     * Matches the given message string against pre-loaded function calling rules.
     * Returns the first resolved response string with placeholders replaced, or null if no match found.
     *
     * @param player The player who sent the message.
     * @param msg    The raw input message.
     * @return A resolved response string or {@code null} if no match found.
     */
    public String getMessageMatch(Player player, String msg) {
        if (functionCallingLoader == null) {
            if (logger != null) {
                logger.warning("FunctionCallingLoader not initialized. Call initializeFunctionCallingLoader() first.");
            }
            return null;
        }

        List<String> matches = functionCallingLoader.match(player, msg);
        return matches.isEmpty() ? null : matches.get(0);
    }

    /**
     * Registers a model under the specified platform if not already registered.
     *
     * @param plugin   The Bukkit plugin instance.
     * @param platform The platform name (e.g., {@code openai}, {@code customurl}).
     * @param model    The model name or {@code server:model} if custom.
     */
    public void registerModel(Plugin plugin, String platform, String model) {
        MCEngineArtificialIntelligenceApiUtilAi.registerModel(plugin, platform, model);
    }

    /**
     * Retrieves an AI model instance by platform and model name.
     *
     * @param platform The platform name.
     * @param model    The model name.
     * @return The model interface instance.
     */
    public IMCEngineArtificialIntelligenceApiModel getAi(String platform, String model) {
        return MCEngineArtificialIntelligenceApiUtilAi.getAi(platform, model);
    }

    /**
     * Returns all registered AI models grouped by platform and model name.
     *
     * @return A nested map of platform → model → model instance.
     */
    @SuppressWarnings("unchecked")
    public Map<String, Map<String, IMCEngineArtificialIntelligenceApiModel>> getAiAll() {
        return (Map<String, Map<String, IMCEngineArtificialIntelligenceApiModel>>) (Map<?, ?>)
                MCEngineArtificialIntelligenceApiUtilAi.getAllModels();
    }

    /**
     * Sends a prompt to the specified model and receives a raw JSON response.
     *
     * @param platform     The AI platform name.
     * @param model        The model name.
     * @param systemPrompt The system prompt providing context or behavior instructions.
     * @param message      The message to send.
     * @return A {@link JsonObject} representing the full JSON response.
     */
    public JsonObject getResponse(String platform, String model, String systemPrompt, String message) {
        return getAi(platform, model).getResponse(systemPrompt, message);
    }

    /**
     * Sends a prompt to the specified model using a custom token and receives a raw JSON response.
     *
     * @param platform     The AI platform name.
     * @param model        The model name.
     * @param token        The token to authorize the request.
     * @param systemPrompt The system prompt providing context or behavior instructions.
     * @param message      The prompt to send to the AI.
     * @return A {@link JsonObject} representing the full JSON response.
     */
    public JsonObject getResponse(String platform, String model, String token, String systemPrompt, String message) {
        return getAi(platform, model).getResponse(token, systemPrompt, message);
    }

    /**
     * Executes an AI bot task asynchronously with the given input.
     *
     * @param plugin    The Bukkit plugin instance.
     * @param db        The database interface used to store AI data.
     * @param player    The player who initiated the task.
     * @param tokenType The type of token to use, either {@code "server"} or {@code "player"}.
     * @param platform  The AI platform name.
     * @param model     The AI model name.
     * @param message   The prompt to send to the AI.
     */
    public void runBotTask(Plugin plugin, IMCEngineArtificialIntelligenceDB db, Player player, String tokenType, String platform, String model, String message) {
        new MCEngineArtificialIntelligenceApiUtilBotTask(plugin, this, db, tokenType, player, platform, model, message)
                .runTaskAsynchronously(plugin);
    }

    /**
     * Sets the waiting status of a player in an AI interaction.
     *
     * @param player  The player.
     * @param waiting {@code true} if the player is waiting for a response; otherwise {@code false}.
     */
    public void setWaiting(Player player, boolean waiting) {
        MCEngineArtificialIntelligenceApiUtilBotManager.setWaiting(player, waiting);
    }

    /**
     * Checks whether the specified player is currently waiting for an AI response.
     *
     * @param player The player to check.
     * @return {@code true} if the player is waiting; {@code false} otherwise.
     */
    public boolean checkWaitingPlayer(Player player) {
        return MCEngineArtificialIntelligenceApiUtilBotManager.isWaiting(player);
    }

    /**
     * Extracts the response content from a full JSON object returned by the AI API.
     *
     * @param responseJson The full JSON response.
     * @return The message content as plain text, or fallback string on error.
     */
    public String getCompletionContent(JsonObject responseJson) {
        return MCEngineArtificialIntelligenceApiUtilAi.getCompletionContent(responseJson);
    }

    /**
     * Extracts the total token usage from a full JSON response.
     *
     * @param responseJson The full JSON response.
     * @return The total number of tokens used, or -1 if unavailable.
     */
    public int getTotalTokenUsage(JsonObject responseJson) {
        return MCEngineArtificialIntelligenceApiUtilAi.getTotalTokenUsage(responseJson);
    }
}
