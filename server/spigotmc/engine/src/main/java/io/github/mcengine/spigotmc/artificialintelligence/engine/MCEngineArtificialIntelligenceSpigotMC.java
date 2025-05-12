package io.github.mcengine.spigotmc.artificialintelligence.engine;

import io.github.mcengine.api.artificialintelligence.MCEngineArtificialIntelligenceApi;
import io.github.mcengine.api.artificialintelligence.util.MCEngineArtificialIntelligenceApiUtilToken;
import io.github.mcengine.common.artificialintelligence.command.MCEngineArtificialIntelligenceCommonCommand;
import io.github.mcengine.common.artificialintelligence.tabcompleter.MCEngineArtificialIntelligenceCommonTabCompleter;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Date;

/**
 * Main SpigotMC plugin class for MCEngineArtificialIntelligence.
 * Handles plugin lifecycle, token validation, API initialization, and update checking.
 */
public class MCEngineArtificialIntelligenceSpigotMC extends JavaPlugin {

    /**
     * Secret key used for token validation (may be configured).
     */
    private String secretKey;

    /**
     * Token used to verify license or authentication.
     */
    private String token;

    /**
     * Expiration date of the token (if applicable).
     */
    private Date expirationDate;

    /**
     * Called when the plugin is enabled.
     * Performs configuration loading, token validation, API initialization, and schedules token validation checks.
     */
    @Override
    public void onEnable() {
        saveDefaultConfig(); // Save config.yml if it doesn't exist

        boolean enabled = getConfig().getBoolean("enable", false);
        if (!enabled) {
            getLogger().warning("Plugin is disabled in config.yml (enable: false). Disabling plugin...");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        MCEngineArtificialIntelligenceApiUtilToken.initialize(this);
        MCEngineArtificialIntelligenceApi api = new MCEngineArtificialIntelligenceApi(this);

        getCommand("ai").setExecutor(new MCEngineArtificialIntelligenceCommonCommand(api));
        getCommand("ai").setTabCompleter(new MCEngineArtificialIntelligenceCommonTabCompleter(this));


        api.checkUpdate("github", "MCEngine", "artificialintelligence", getConfig().getString("github.token", "null"));
    }

    /**
     * Called when the plugin is disabled.
     */
    @Override
    public void onDisable() {}
}
