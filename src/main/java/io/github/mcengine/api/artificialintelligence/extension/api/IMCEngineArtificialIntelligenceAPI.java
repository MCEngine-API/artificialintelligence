package io.github.mcengine.api.artificialintelligence.extension.api;

import org.bukkit.plugin.Plugin;

/**
 * Represents a core AI API module that can be dynamically loaded into the MCEngine.
 * <p>
 * Implement this interface to provide AI capabilities to the core system.
 */
public interface IMCEngineArtificialIntelligenceAPI {

    /**
     * Called when the AI API module is loaded by the engine.
     *
     * @param plugin The plugin instance providing context.
     */
    void onLoad(Plugin plugin);

    /**
     * Sets a unique ID for this AI API module.
     *
     * @param id The unique ID assigned by the engine.
     */
    void setId(String id);
}
