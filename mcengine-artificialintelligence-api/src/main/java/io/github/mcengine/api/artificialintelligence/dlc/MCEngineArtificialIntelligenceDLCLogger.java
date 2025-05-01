package io.github.mcengine.api.artificialintelligence.dlc;

import org.bukkit.plugin.Plugin;

import java.util.logging.Logger;

public class MCEngineArtificialIntelligenceDLCLogger {

    private final Logger logger;
    private final String dlcName;

    public MCEngineArtificialIntelligenceDLCLogger(Plugin plugin, String dlcName) {
        this.logger = plugin.getLogger();
        this.dlcName = dlcName;
    }

    public void info(String message) {
        logger.info("[ DLC ] [ " + dlcName + " ] " + message);
    }

    public void warning(String message) {
        logger.warning("[ DLC ] [ " + dlcName + " ] " + message);
    }

    public void severe(String message) {
        logger.severe("[ DLC ] [ " + dlcName + " ] " + message);
    }
}
