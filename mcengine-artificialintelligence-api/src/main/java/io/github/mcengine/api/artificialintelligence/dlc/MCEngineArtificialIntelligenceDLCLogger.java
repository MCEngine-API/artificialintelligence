package io.github.mcengine.api.artificialintelligence.dlc;

import org.bukkit.plugin.Plugin;

import java.util.logging.Logger;

/**
 * Logger utility for AI DLCs, providing prefixed logging for a specific DLC name.
 */
public class MCEngineArtificialIntelligenceDLCLogger {

    private final Logger logger;
    private final String dlcName;

    /**
     * Constructs a new DLC logger for the specified plugin and DLC name.
     *
     * @param plugin  The plugin instance to retrieve the logger from.
     * @param dlcName The name of the DLC to include in log messages.
     */
    public MCEngineArtificialIntelligenceDLCLogger(Plugin plugin, String dlcName) {
        this.logger = plugin.getLogger();
        this.dlcName = dlcName;
    }

    /**
     * Logs an informational message with the DLC prefix.
     *
     * @param message The message to log.
     */
    public void info(String message) {
        logger.info("[ DLC ] [ " + dlcName + " ] " + message);
    }

    /**
     * Logs a warning message with the DLC prefix.
     *
     * @param message The message to log.
     */
    public void warning(String message) {
        logger.warning("[ DLC ] [ " + dlcName + " ] " + message);
    }

    /**
     * Logs a severe error message with the DLC prefix.
     *
     * @param message The message to log.
     */
    public void severe(String message) {
        logger.severe("[ DLC ] [ " + dlcName + " ] " + message);
    }
}
