package io.github.mcengine.spigotmc.artificialintelligence.engine;

import io.github.mcengine.api.artificialintelligence.MCEngineArtificialIntelligenceApi;
import io.github.mcengine.spigotmc.artificialintelligence.engine.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Date;

public class MCEngineArtificialIntelligenceSpigotMC extends JavaPlugin {

    private String secretKey;
    private String token;
    private Date expirationDate;
    private MCEngineArtificialIntelligenceApi api; // <-- Add this

    @Override
    public void onEnable() {
        saveDefaultConfig(); // Save config.yml if it doesn't exist

        // Initialize bStats metrics
        new Metrics(this, 25556);

        secretKey = getConfig().getString("secretKey", "mcengine");
        token = getConfig().getString("token", "");

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

        getLogger().info("✅ Token validated successfully!");

        api = new MCEngineArtificialIntelligenceApi(this); // <-- Initialize API
        scheduleMidnightCheck();
    }

    private boolean validateToken() {
        return MCEngineArtificialIntelligenceApi.validateToken(getName(), secretKey, token, new Date());
    }

    private void scheduleMidnightCheck() {
        long delay = calculateDelayUntilMidnight();
        long period = 24L * 60L * 60L * 20L; // 24 hours in ticks (20 ticks/sec)

        Bukkit.getScheduler().runTaskTimer(this, () -> {
            if (!validateToken()) {
                getLogger().warning("❌ Token expired or invalid at midnight! Disabling plugin...");
                getServer().getPluginManager().disablePlugin(this);
            }
        }, delay, period);
    }

    private long calculateDelayUntilMidnight() {
        long now = System.currentTimeMillis();
        long tomorrowMidnight = ((now / (24L * 60L * 60L * 1000L)) + 1L) * (24L * 60L * 60L * 1000L);
        return (tomorrowMidnight - now) / 50L; // Convert ms to ticks
    }

    /**
     * Get the API instance of this plugin.
     */
    public MCEngineArtificialIntelligenceApi getApi() {
        return api;
    }
}
