package io.github.mcengine.api.artificialintelligence;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.sql.Connection;
import java.util.Map;

import io.github.mcengine.api.artificialintelligence.database.IMCEngineArtificialIntelligenceApiDatabase;
import io.github.mcengine.api.artificialintelligence.database.sqlite.MCEngineArtificialIntelligenceApiDBSQLite;
import io.github.mcengine.api.artificialintelligence.model.*;
import io.github.mcengine.api.artificialintelligence.util.*;

/**
 * Main API class for MCEngineArtificialIntelligence.
 * Handles AI model initialization, extension loading (AddOns/DLCs), and token validation.
 */
public class MCEngineArtificialIntelligenceApi {

    /**
     * Singleton instance of the API.
     */
    private static MCEngineArtificialIntelligenceApi instance;

    /**
     * Database handler instance for storing and retrieving player tokens.
     */
    private final IMCEngineArtificialIntelligenceApiDatabase db;

    /**
     * The Bukkit plugin instance associated with this AI API.
     */
    private final Plugin plugin;

    /**
     * Constructs a new AI API instance with the given plugin.
     * Initializes the database handler and loads supported AI models and extensions.
     * <p>
     * Supported model configuration keys:
     * <ul>
     *   <li>{@code ai.{platform}.models}</li>
     *   <li>{@code ai.custom.{server}.models}</li>
     * </ul>
     * Supported platforms: {@code deepseek}, {@code openai}, {@code openrouter}, {@code customurl}.
     *
     * @param plugin The Bukkit plugin instance.
     */
    public MCEngineArtificialIntelligenceApi(Plugin plugin) {
        instance = this;
        this.plugin = plugin;

        // Initialize database based on type
        String dbType = plugin.getConfig().getString("database.type", "sqlite").toLowerCase();
        switch (dbType) {
            case "sqlite":
                this.db = new MCEngineArtificialIntelligenceApiDBSQLite(plugin);
                break;
            default:
                throw new IllegalArgumentException("Unsupported database type: " + dbType);
        }
    }

    /**
     * Returns the global API singleton instance.
     *
     * @return The {@link MCEngineArtificialIntelligenceApi} instance.
     */
    public static MCEngineArtificialIntelligenceApi getApi() {
        return instance;
    }

    /**
     * Returns the Bukkit plugin instance linked to this API.
     *
     * @return The plugin instance.
     */
    public Plugin getPlugin() {
        return plugin;
    }

    /**
     * Returns the database handler implementation.
     *
     * @return The database API implementation.
     */
    public IMCEngineArtificialIntelligenceApiDatabase getDB() {
        return db;
    }

    /**
     * Retrieves the active database connection used by the AI plugin.
     * <p>
     * This delegates to the underlying database implementation such as SQLite.
     * Useful for executing custom SQL queries or diagnostics externally.
     *
     * @return The {@link Connection} instance for the configured database.
     */
    public Connection getDBConnection() {
        return db.getDBConnection();
    }

    /**
     * Stores or updates a player's API token for a given AI platform.
     *
     * @param playerUuid The UUID of the player.
     * @param platform   The platform name (e.g., {@code openai}).
     * @param token      The raw token to store.
     */
    public void setPlayerToken(String playerUuid, String platform, String token) {
        db.setPlayerToken(playerUuid, platform, token);
    }

    /**
     * Retrieves the stored token for a given player and platform.
     *
     * @param playerUuid The UUID of the player.
     * @param platform   The platform name.
     * @return The encrypted token, or {@code null} if not found.
     */
    public String getPlayerToken(String playerUuid, String platform) {
        return db.getPlayerToken(playerUuid, platform);
    }

    /**
     * Registers a model under the specified platform if not already registered.
     *
     * @param platform The platform name (e.g., {@code openai}, {@code customurl}).
     * @param model    The model name or {@code server:model} if custom.
     */
    public void registerModel(String platform, String model) {
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
    public void runBotTask(Player player, String tokenType, String platform, String model, String message) {
        new MCEngineArtificialIntelligenceApiUtilBotTask(plugin, tokenType, player, platform, model, message)
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
