package io.github.mcengine.api.artificialintelligence.extension.agent;

import org.bukkit.plugin.Plugin;

/**
 * Represents an AI-based agent module that can be dynamically loaded into the MCEngine.
 * <p>
 * Implement this interface to integrate AI-related agents into the plugin system.
 */
public interface IMCEngineArtificialIntelligenceAgent {

    /**
     * Invoked when the AI agent is loaded by the engine.
     *
     * @param plugin the plugin instance providing the runtime context; never {@code null}
     */
    void onLoad(Plugin plugin);

    /**
     * Invoked when the agent is unloaded or disabled by the engine.
     * <p>
     * Implementations should release resources and revert transient state here.
     *
     * @param plugin the plugin instance providing the runtime context; never {@code null}
     */
    void onDisload(Plugin plugin);

    /**
     * Assigns the unique identifier for this agent instance.
     *
     * @param id the unique identifier assigned by the engine; never {@code null} or empty
     */
    void setId(String id);
}
