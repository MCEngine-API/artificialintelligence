package io.github.mcengine.api.artificialintelligence;

import org.bukkit.plugin.Plugin;
import org.bukkit.configuration.ConfigurationSection;

import java.text.ParseException;
import java.util.Date;
import java.util.logging.Logger;

import io.github.mcengine.api.artificialintelligence.database.IMCEngineArtificialIntelligenceApiDatabase;
import io.github.mcengine.api.artificialintelligence.database.sqlite.MCEngineArtificialIntelligenceApiDatabaseSQLite;
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
     * Logger for logging plugin-related messages.
     */
    private final Logger logger;

    /**
     * Constructs a new AI API instance with the given plugin.
     * Initializes the AI model and loads addons and DLCs.
     *
     * @param plugin The Bukkit plugin instance.
     */
    public MCEngineArtificialIntelligenceApi(Plugin plugin) {
        instance = this;
        new Metrics(plugin, 25556);
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        loadAddOns();
        loadDLCs();

        // Initialize database based on type
        String dbType = plugin.getConfig().getString("database.type", "sqlite").toLowerCase();
        switch (dbType) {
            case "sqlite":
                this.db = new MCEngineArtificialIntelligenceApiDatabaseSQLite(plugin);
                break;
            default:
                throw new IllegalArgumentException("Unsupported database type: " + dbType);
        }

        // Load models for deepseek, openai, openrouter (multiple model config)
        String[] platforms = { "deepseek", "openai", "openrouter" };
        for (String platform : platforms) {
            String modelsKey = "ai." + platform + ".models";
            if (plugin.getConfig().isConfigurationSection(modelsKey)) {
                ConfigurationSection section = plugin.getConfig().getConfigurationSection(modelsKey);
                for (String key : section.getKeys(false)) {
                    String modelName = section.getString(key);
                    if (modelName != null && !modelName.equalsIgnoreCase("null")) {
                        registerModel(platform, modelName);
                    }
                }
            }
        }

        // Load models for customurl (multiple servers and models)
        if (plugin.getConfig().isConfigurationSection("ai.custom")) {
            for (String server : plugin.getConfig().getConfigurationSection("ai.custom").getKeys(false)) {
                String modelsKey = "ai.custom." + server + ".models";
                if (plugin.getConfig().isConfigurationSection(modelsKey)) {
                    ConfigurationSection section = plugin.getConfig().getConfigurationSection(modelsKey);
                    for (String key : section.getKeys(false)) {
                        String modelName = section.getString(key);
                        if (modelName != null && !modelName.equalsIgnoreCase("null")) {
                            registerModel("customurl", server + ":" + modelName);
                        }
                    }
                }
            }
        }
    }

    /**
     * Gets the global API instance.
     *
     * @return The {@link MCEngineArtificialIntelligenceApi} singleton instance.
     */
    public static MCEngineArtificialIntelligenceApi getApi() {
        return instance;
    }

    /**
     * Gets the associated plugin instance.
     *
     * @return The plugin instance.
     */
    public Plugin getPlugin() {
        return plugin;
    }

    public IMCEngineArtificialIntelligenceApiDatabase getDB() {
        return db;
    }

    /**
     * Checks for updates by querying the specified Git platform (GitHub, GitLab, etc.)
     * using the organization, repository, and token provided.
     * This can be used to inform server owners or developers when a new plugin version is available.
     *
     * @param gitPlatform The Git platform to use (e.g., "github", "gitlab").
     * @param org         The organization or user owning the repository.
     * @param repository  The repository name.
     * @param token       The access token to authenticate with the platform API (can be optional for public repos).
     */
    public void checkUpdate(String gitPlatform, String org, String repository, String token) {
        MCEngineArtificialIntelligenceApiUtilUpdate.checkUpdate(plugin, gitPlatform, org, repository, token);
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
     * Sets a player-specific token for a given platform.
     *
     * @param playerUuid The player's UUID.
     * @param platform   The platform name.
     * @param token      The token to store.
     */
    public void setPlayerToken(String playerUuid, String platform, String token) {
        db.setPlayerToken(playerUuid, platform, token);
    }

    /**
     * Retrieves a player-specific token for a given platform.
     *
     * @param playerUuid The player's UUID.
     * @param platform   The platform name.
     * @return The stored token or null if not found.
     */
    public String getPlayerToken(String playerUuid, String platform) {
        return db.getPlayerToken(playerUuid, platform);
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
     * Shortcut to get response from AI using a token.
     *
     * @param platform AI platform
     * @param model    model name
     * @param token    API token or user-specific key
     * @param message  prompt message
     * @return response from AI
     */
    public String getResponse(String platform, String model, String token, String message) {
        return getAi(platform, model).getResponse(token, message);
    }
}
