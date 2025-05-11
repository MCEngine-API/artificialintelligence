package io.github.mcengine.api.artificialintelligence.addon;

import org.bukkit.plugin.Plugin;

import java.util.logging.Logger;

/**
 * Logger utility for AI AddOns, providing prefixed logging for a specific AddOn name.
 */
public class MCEngineArtificialIntelligenceAddOnLogger {

    private final Logger logger;
    private final String addOnName;

    /**
     * Constructs a new AddOn logger for the specified plugin and AddOn name.
     *
     * @param plugin     The plugin instance to retrieve the logger from.
     * @param addOnName  The name of the AddOn to include in log messages.
     */
    public MCEngineArtificialIntelligenceAddOnLogger(Plugin plugin, String addOnName) {
        this.logger = plugin.getLogger();
        this.addOnName = addOnName;
    }

    /**
     * Logs an informational message with the AddOn prefix.
     *
     * @param message The message to log.
     */
    public void info(String message) {
        logger.info("[AddOn] [" + addOnName + "] " + message);
    }

    /**
     * Logs a warning message with the AddOn prefix.
     *
     * @param message The message to log.
     */
    public void warning(String message) {
        logger.warning("[AddOn] [" + addOnName + "] " + message);
    }

    /**
     * Logs a severe error message with the AddOn prefix.
     *
     * @param message The message to log.
     */
    public void severe(String message) {
        logger.severe("[AddOn] [" + addOnName + "] " + message);
    }
}
