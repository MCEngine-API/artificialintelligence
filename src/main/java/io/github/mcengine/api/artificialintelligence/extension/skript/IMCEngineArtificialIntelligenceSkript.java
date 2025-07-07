package io.github.mcengine.api.artificialintelligence.extension.skript;

import org.bukkit.plugin.Plugin;

/**
 * Interface for Skript modules that can be dynamically loaded.
 */
public interface IMCEngineArtificialIntelligenceSkript {

    /**
     * Called when the Skript is loaded by the engine.
     *
     * @param plugin The plugin instance providing context.
     */
    void onLoad(Plugin plugin);
}
