package io.github.mcengine.api.artificialintelligence.extension.skript;

import org.bukkit.plugin.Plugin;

/**
 * Represents a skript-based AI module that can be dynamically loaded into the MCEngine.
 * <p>
 * These modules typically encapsulate custom scripted AI behavior and are used to extend game logic.
 */
public interface IMCEngineArtificialintelligenceSkript {

    /**
     * Called when the AI skript module is loaded by the engine.
     *
     * @param plugin The plugin instance providing context.
     */
    void onLoad(Plugin plugin);

    /**
     * Sets a unique ID for this AI skript module.
     *
     * @param id The unique ID assigned by the engine.
     */
    void setId(String id);
}
