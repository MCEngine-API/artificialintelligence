package io.github.mcengine.api.artificialintelligence.extension.library;

import org.bukkit.plugin.Plugin;

/**
 * Represents a library for AI modules that can be dynamically loaded into the MCEngine.
 * <p>
 * Typically used to support AI backend logic or infrastructure without player interaction.
 */
public interface IMCEngineArtificialIntelligenceLibrary {

    /**
     * Called when the AI library is loaded by the engine.
     *
     * @param plugin The plugin instance providing context.
     */
    void onLoad(Plugin plugin);

    /**
     * Sets a unique ID for this AI library module.
     *
     * @param id The unique ID assigned by the engine.
     */
    void setId(String id);
}
