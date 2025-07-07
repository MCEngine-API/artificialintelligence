package io.github.mcengine.api.artificialintelligence.extension.library;

import org.bukkit.plugin.Plugin;

/**
 * Interface for Library modules that can be dynamically loaded.
 */
public interface IMCEngineArtificialIntelligenceLibrary {

    /**
     * Called when the Library is loaded by the engine.
     *
     * @param plugin The plugin instance providing context.
     */
    void onLoad(Plugin plugin);
}
