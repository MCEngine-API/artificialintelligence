package io.github.mcengine.api.artificialintelligence.extension.api;

import org.bukkit.plugin.Plugin;

/**
 * Interface for API modules that can be dynamically loaded.
 */
public interface IMCEngineArtificialIntelligenceAPI {

    /**
     * Called when the API is loaded by the engine.
     *
     * @param plugin The plugin instance providing context.
     */
    void onLoad(Plugin plugin);
}
