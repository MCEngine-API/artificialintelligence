package io.github.mcengine.spigotmc.artificialintelligence.engine;

import io.github.mcengine.api.artificialintelligence.MCEngineArtificialIntelligenceApi;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Date;

/**
 * Main SpigotMC plugin class for MCEngineArtificialIntelligence.
 * Handles plugin lifecycle, token validation, API initialization, and update checking.
 */
public class MCEngineArtificialIntelligenceSpigotMC extends JavaPlugin {

    private String secretKey;
    private String token;
    private Date expirationDate;
    private MCEngineArtificialIntelligenceApi api;

    /**
     * Called when the plugin is enabled.
     * Performs configuration loading, token validation, API initialization, and schedules token validation checks.
     */
    @Override
    public void onEnable() {
        saveDefaultConfig(); // Save config.yml if it doesn't exist

        /* secretKey = getConfig().getString("secretKey", "mcengine");
        token = getConfig().getString("token", "");

        boolean enabled = getConfig().getBoolean("enable", false);
        if (!enabled) {
            getLogger().warning("Plugin is disabled in config.yml (enable: false). Disabling plugin...");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        if (token == null || token.isEmpty()) {
            getLogger().warning("No token found in config.yml!");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        expirationDate = MCEngineArtificialIntelligenceApi.extractExpirationDate(getName(), secretKey, token);
        if (expirationDate == null || expirationDate.getTime() == 0L) {
            getLogger().warning("Failed to extract expiration date from token!");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        if (!validateToken()) {
            getLogger().warning("Token validation failed or expired!");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        getLogger().info("Token validated successfully!"); */

        api = new MCEngineArtificialIntelligenceApi(this);
        scheduleMidnightCheck();
        api.checkUpdate("github", "MCEngine", "artificialintelligence", getConfig().getString("github.token", "null"));
    }

    @Override
    public void onDisable() {
        getLogger().info("MCEngineArtificialIntelligenceSpigotMC has been disabled.");
    }

    /**
     * Validates the current token using the API's validation method.
     *
     * @return true if the token is valid and not expired; false otherwise.
     */
    private boolean validateToken() {
        return MCEngineArtificialIntelligenceApi.validateToken(getName(), secretKey, token, new Date());
    }

    /**
     * Schedules a daily token validation check at midnight.
     * If the token is invalid at midnight, the plugin will disable itself.
     */
    private void scheduleMidnightCheck() {
        long delay = calculateDelayUntilMidnight();
        long period = 24L * 60L * 60L * 20L; // 24 hours in ticks (20 ticks/sec)

        Bukkit.getScheduler().runTaskTimer(this, () -> {
            if (!validateToken()) {
                getLogger().warning("Token expired or invalid at midnight! Disabling plugin...");
                getServer().getPluginManager().disablePlugin(this);
            }
        }, delay, period);
    }

    /**
     * Calculates the number of server ticks until midnight.
     *
     * @return The delay in ticks until the next midnight.
     */
    private long calculateDelayUntilMidnight() {
        long now = System.currentTimeMillis();
        long tomorrowMidnight = ((now / (24L * 60L * 60L * 1000L)) + 1L) * (24L * 60L * 60L * 1000L);
        return (tomorrowMidnight - now) / 50L; // Convert ms to ticks
    }

    /**
     * Gets the API instance associated with this plugin.
     *
     * @return The {@link MCEngineArtificialIntelligenceApi} instance.
     */
    public MCEngineArtificialIntelligenceApi getApi() {
        return api;
    }
}
