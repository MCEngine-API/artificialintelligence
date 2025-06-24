package io.github.mcengine.api.artificialintelligence;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.sql.Connection;
import java.util.Map;

import io.github.mcengine.api.artificialintelligence.database.IMCEngineArtificialIntelligenceDB;

import io.github.mcengine.api.artificialintelligence.model.*;
import io.github.mcengine.api.artificialintelligence.util.*;

/**
 * Main API class for MCEngineArtificialIntelligence.
 * Handles AI model initialization, extension loading (AddOns/DLCs), and token validation.
 */
public class MCEngineArtificialIntelligenceApi {

    /**
     * Registers a model under the specified platform if not already registered.
     *
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
     * @return The model interface, or {@code null} if not registered.
     */
    public IMCEngineArtificialIntelligenceApiModel getAi(String platform, String model) {
        return MCEngineArtificialIntelligenceApiUtilAi.getAi(platform, model);
    }

    /**
     * Returns all registered AI models grouped by platform and model name.
     *
     * @return A nested map of platform → model → model instance.
     */
    public Map<String, Map<String, IMCEngineArtificialIntelligenceApiModel>> getAiAll() {
        //noinspection unchecked
        return (Map<String, Map<String, IMCEngineArtificialIntelligenceApiModel>>) (Map<?, ?>)
                MCEngineArtificialIntelligenceApiUtilAi.getAllModels();
    }

    /**
     * Gets a direct response from a registered AI model.
     *
     * @param platform The AI platform.
     * @param model    The model name.
     * @param message  The prompt to send.
     * @return The AI-generated response.
     */
    public String getResponse(String platform, String model, String message) {
        return getAi(platform, model).getResponse(message);
    }

    /**
     * Gets a direct response from an AI model using a provided token.
     *
     * @param platform The AI platform.
     * @param model    The model name.
     * @param token    The API key or personal token.
     * @param message  The prompt to send.
     * @return The AI-generated response.
     */
    public String getResponse(String platform, String model, String token, String message) {
        return getAi(platform, model).getResponse(token, message);
    }

    /**
     * Executes an AI bot task asynchronously with the given input.
     *
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
}
