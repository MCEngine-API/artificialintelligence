package io.github.mcengine.api.artificialintelligence;

import org.bukkit.plugin.Plugin;

import java.util.Map;

import io.github.mcengine.api.artificialintelligence.database.IMCEngineArtificialIntelligenceApiDatabase;
import io.github.mcengine.api.artificialintelligence.database.sqlite.MCEngineArtificialIntelligenceApiDatabaseSQLite;
import io.github.mcengine.api.artificialintelligence.model.*;
import io.github.mcengine.api.artificialintelligence.util.*;
import io.github.mcengine.api.mcengine.util.*;

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
     * Initializes the AI model and loads addons and DLCs from the filesystem.
     *
     * Also loads supported models from config:
     * - ai.{platform}.models
     * - ai.custom.{server}.models
     *
     * Supports platforms: deepseek, openai, openrouter, customurl.
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
                this.db = new MCEngineArtificialIntelligenceApiDatabaseSQLite(plugin);
                break;
            default:
                throw new IllegalArgumentException("Unsupported database type: " + dbType);
        }
    }

    /**
     * Gets the global API singleton instance.
     *
     * @return The {@link MCEngineArtificialIntelligenceApi} instance.
     */
    public static MCEngineArtificialIntelligenceApi getApi() {
        return instance;
    }

    /**
     * Gets the Bukkit plugin instance linked to this API.
     *
     * @return The plugin instance.
     */
    public Plugin getPlugin() {
        return plugin;
    }

    /**
     * Gets the database handler implementation.
     *
     * @return The database API implementation.
     */
    public IMCEngineArtificialIntelligenceApiDatabase getDB() {
        return db;
    }

    /**
     * Checks for updates by querying the specified Git platform.
     * Logs to console if a new version is available.
     *
     * @param gitPlatform The platform to query ("github" or "gitlab").
     * @param org         The organization or user.
     * @param repository  The repository name.
     * @param token       The access token (nullable).
     */
    public void checkUpdate(String gitPlatform, String org, String repository, String token) {
        MCEngineApiUtilUpdate.checkUpdate(plugin, gitPlatform, org, repository, token);
    }

    /**
     * Stores or updates a player's API token for a given AI platform.
     *
     * @param playerUuid The UUID of the player.
     * @param platform   The platform name (e.g., "openai").
     * @param token      The raw token to store.
     */
    public void setPlayerToken(String playerUuid, String platform, String token) {
        db.setPlayerToken(playerUuid, platform, token);
    }

    /**
     * Retrieves a stored token for a given player and platform.
     *
     * @param playerUuid The UUID of the player.
     * @param platform   The platform name.
     * @return The encrypted token or null if not found.
     */
    public String getPlayerToken(String playerUuid, String platform) {
        return db.getPlayerToken(playerUuid, platform);
    }

    /**
     * Registers a model under a given platform if not already loaded.
     *
     * @param platform Platform name (e.g., "openai", "customurl").
     * @param model    Model name or server:model if custom.
     */
    public void registerModel(String platform, String model) {
        MCEngineArtificialIntelligenceApiUtilAi.registerModel(plugin, platform, model);
    }

    /**
     * Retrieves an AI model by platform and model name.
     *
     * @param platform The platform name.
     * @param model    The model name.
     * @return The model interface or null if not registered.
     */
    public IMCEngineArtificialIntelligenceApiModel getAi(String platform, String model) {
        return MCEngineArtificialIntelligenceApiUtilAi.getAi(platform, model);
    }

    /**
     * Gets all registered AI models mapped by platform and model name.
     *
     * @return A nested map of platform -> model -> model instance.
     */
    public Map<String, Map<String, IMCEngineArtificialIntelligenceApiModel>> getAiAll() {
        //noinspection unchecked
        return (Map<String, Map<String, IMCEngineArtificialIntelligenceApiModel>>) (Map<?, ?>)
                MCEngineArtificialIntelligenceApiUtilAi.getAllModels();
    }

    /**
     * Gets a direct response from the AI using a registered model.
     *
     * @param platform The AI platform.
     * @param model    The model name.
     * @param message  The prompt to send.
     * @return The AI response.
     */
    public String getResponse(String platform, String model, String message) {
        return getAi(platform, model).getResponse(message);
    }

    /**
     * Gets a direct response from the AI using a provided token.
     *
     * @param platform The AI platform.
     * @param model    The model name.
     * @param token    The API key or personal token.
     * @param message  The prompt to send.
     * @return The AI response.
     */
    public String getResponse(String platform, String model, String token, String message) {
        return getAi(platform, model).getResponse(token, message);
    }

    /**
     * Executes an AI bot task asynchronously with the given input.
     *
     * @param player   The player sending the message.
     * @param platform The AI platform name.
     * @param model    The AI model name.
     * @param message  The message to send.
     */
    public void runBotTask(Player player, String platform, String model, String message) {
        new MCEngineArtificialIntelligenceApiUtilBotTask(plugin, player, platform, model, message)
            .runTaskAsynchronously(plugin);
    }

    /**
     * Sets the waiting status of a player in an AI conversation.
     *
     * @param player  The player.
     * @param waiting Whether the player is waiting.
     */
    public void setWaiting(Player player, boolean waiting) {
        MCEngineArtificialIntelligenceApiUtilBotManager.setWaiting(player, waiting);
    }
}
