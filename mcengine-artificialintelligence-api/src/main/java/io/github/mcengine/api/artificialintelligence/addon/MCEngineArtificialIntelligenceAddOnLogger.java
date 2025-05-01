package io.github.mcengine.api.artificialintelligence.addon;

import org.bukkit.plugin.Plugin;

import java.util.logging.Logger;

public class MCEngineArtificialIntelligenceAddOnLogger {

    private final Logger logger;
    private final String addOnName;

    public MCEngineArtificialIntelligenceAddOnLogger(Plugin plugin, String addOnName) {
        this.logger = plugin.getLogger();
        this.addOnName = addOnName;
    }

    public void info(String message) {
        logger.info("[ AddOn ] [ " + addOnName + " ] " + message);
    }

    public void warning(String message) {
        logger.warning("[ AddOn ] [ " + addOnName + " ] " + message);
    }

    public void severe(String message) {
        logger.severe("[ AddOn ] [ " + addOnName + " ] " + message);
    }
}
