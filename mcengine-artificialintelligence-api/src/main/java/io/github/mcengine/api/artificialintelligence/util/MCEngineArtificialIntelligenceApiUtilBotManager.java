package io.github.mcengine.api.artificialintelligence.util;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * API utility class to manage AI chat conversations for each player.
 * <p>
 * Handles:
 * <ul>
 *     <li>Conversation history tracking</li>
 *     <li>Player session activation/deactivation</li>
 *     <li>Waiting state tracking</li>
 *     <li>Model and platform assignments per player</li>
 * </ul>
 * This is designed to be shared across plugins and addons.
 */
public class MCEngineArtificialIntelligenceApiUtilBotManager {

    /**
     * Stores the conversation history per player UUID.
     */
    private static final Map<UUID, StringBuilder> playerConversations = new ConcurrentHashMap<>();

    /**
     * Tracks players with active AI sessions.
     */
    private static final Set<UUID> activePlayers = ConcurrentHashMap.newKeySet();

    /**
     * Tracks players currently waiting for an AI response.
     */
    private static final Set<UUID> waitingPlayers = ConcurrentHashMap.newKeySet();

    /**
     * Maps player UUIDs to the AI platform in use.
     */
    private static final Map<UUID, String> playerPlatform = new ConcurrentHashMap<>();

    /**
     * Maps player UUIDs to the AI model in use.
     */
    private static final Map<UUID, String> playerModel = new ConcurrentHashMap<>();

    /**
     * Initializes a new conversation for the given player.
     *
     * @param player The player to start a conversation for.
     */
    public static void startConversation(Player player) {
        playerConversations.put(player.getUniqueId(), new StringBuilder());
    }

    /**
     * Appends a message to the player's conversation history.
     *
     * @param player  The player whose history to append to.
     * @param message The message to append.
     */
    public static void append(Player player, String message) {
        playerConversations
            .computeIfAbsent(player.getUniqueId(), k -> new StringBuilder())
            .append(message)
            .append("\n");
    }

    /**
     * Retrieves the entire conversation history for a player.
     *
     * @param player The player whose history to retrieve.
     * @return The conversation as a single string.
     */
    public static String get(Player player) {
        return playerConversations.getOrDefault(player.getUniqueId(), new StringBuilder()).toString();
    }

    /**
     * Ends and removes the conversation history for a player.
     *
     * @param player The player whose conversation to remove.
     */
    public static void end(Player player) {
        playerConversations.remove(player.getUniqueId());
    }

    /**
     * Activates the AI session for a player.
     *
     * @param player The player to activate.
     */
    public static void activate(Player player) {
        activePlayers.add(player.getUniqueId());
    }

    /**
     * Deactivates the AI session for a player.
     *
     * @param player The player to deactivate.
     */
    public static void deactivate(Player player) {
        activePlayers.remove(player.getUniqueId());
    }

    /**
     * Checks whether the player has an active AI session.
     *
     * @param player The player to check.
     * @return True if active; false otherwise.
     */
    public static boolean isActive(Player player) {
        return activePlayers.contains(player.getUniqueId());
    }

    /**
     * Completely terminates a player's session, including conversation, model, and wait state.
     *
     * @param player The player to terminate.
     */
    public static void terminate(Player player) {
        end(player);
        deactivate(player);
        setWaiting(player, false);
        clearModel(player);
    }

    /**
     * Checks whether a player is currently waiting for an AI response.
     *
     * @param player The player to check.
     * @return True if waiting; false otherwise.
     */
    public static boolean isWaiting(Player player) {
        return waitingPlayers.contains(player.getUniqueId());
    }

    /**
     * Sets the player's waiting state.
     *
     * @param player  The player.
     * @param waiting True to mark as waiting; false to clear waiting state.
     */
    public static void setWaiting(Player player, boolean waiting) {
        if (waiting) {
            waitingPlayers.add(player.getUniqueId());
        } else {
            waitingPlayers.remove(player.getUniqueId());
        }
    }

    /**
     * Associates a platform and model with the player.
     *
     * @param player   The player.
     * @param platform The AI platform (e.g., "openai").
     * @param model    The AI model name (e.g., "gpt-4o").
     */
    public static void setModel(Player player, String platform, String model) {
        playerPlatform.put(player.getUniqueId(), platform);
        playerModel.put(player.getUniqueId(), model);
    }

    /**
     * Gets the current AI platform associated with a player.
     *
     * @param player The player.
     * @return The platform name, or "gpt" if not set.
     */
    public static String getPlatform(Player player) {
        return playerPlatform.getOrDefault(player.getUniqueId(), "gpt");
    }

    /**
     * Gets the current AI model associated with a player.
     *
     * @param player The player.
     * @return The model name, or "gpt-4o" if not set.
     */
    public static String getModel(Player player) {
        return playerModel.getOrDefault(player.getUniqueId(), "gpt-4o");
    }

    /**
     * Clears the model and platform settings for a player.
     *
     * @param player The player.
     */
    public static void clearModel(Player player) {
        playerPlatform.remove(player.getUniqueId());
        playerModel.remove(player.getUniqueId());
    }

    /**
     * Terminates all active player AI sessions.
     * This is typically called when the plugin is disabled or reloaded.
     */
    public static void terminateAll() {
        for (UUID uuid : Set.copyOf(activePlayers)) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null && player.isOnline()) {
                terminate(player);
                player.sendMessage("§cYour AI session has ended due to the plugin being reloaded or disabled.");
            }
        }
        playerConversations.clear();
        activePlayers.clear();
        waitingPlayers.clear();
        playerPlatform.clear();
        playerModel.clear();
    }
}
