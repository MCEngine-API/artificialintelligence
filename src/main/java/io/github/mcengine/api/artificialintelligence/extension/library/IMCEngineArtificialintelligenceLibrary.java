package io.github.mcengine.api.artificialintelligence.extension.library;

import org.bukkit.plugin.Plugin;

/**
 * Represents a library for AI modules that can be dynamically loaded into the MCEngine.
 * <p>
 * Typically used to support AI backend logic or infrastructure without player interaction.
 */
public interface IMCEngineArtificialintelligenceLibrary {

    /**
     * Called when the AI library is loaded by the engine.
     *
     * @param plugin The plugin instance providing context.
     */
    void onLoad(Plugin plugin);

    /**
     * Called when the DLC library is unloaded or disabled by the engine.
     * <p>
     * Implementations should use this method to release any services or dependencies registered during {@link #onLoad(Plugin)}.
     *
     * @param plugin The {@link Plugin} instance providing context for this DLC module.
     */
    void onDisload(Plugin plugin);

    /**
     * Sets a unique ID for this AI library module.
     *
     * @param id The unique ID assigned by the engine.
     */
    void setId(String id);
}
