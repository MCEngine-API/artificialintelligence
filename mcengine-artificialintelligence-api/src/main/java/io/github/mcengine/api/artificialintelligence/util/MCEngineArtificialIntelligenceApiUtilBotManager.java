package io.github.mcengine.api.artificialintelligence.util;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * API utility class to manage AI chat conversations for each player.
 * This is designed to be shared across plugins and addons.
 */
public class MCEngineArtificialIntelligenceApiUtilBotManager {

    private static final Map<UUID, StringBuilder> playerConversations = new ConcurrentHashMap<>();
    private static final Set<UUID> activePlayers = ConcurrentHashMap.newKeySet();
    private static final Set<UUID> waitingPlayers = ConcurrentHashMap.newKeySet();
    private static final Map<UUID, String> playerPlatform = new ConcurrentHashMap<>();
    private static final Map<UUID, String> playerModel = new ConcurrentHashMap<>();

    public static void startConversation(Player player) {
        playerConversations.put(player.getUniqueId(), new StringBuilder());
    }

    public static void append(Player player, String message) {
        playerConversations
            .computeIfAbsent(player.getUniqueId(), k -> new StringBuilder())
            .append(message)
            .append("\n");
    }

    public static String get(Player player) {
        return playerConversations.getOrDefault(player.getUniqueId(), new StringBuilder()).toString();
    }

    public static void end(Player player) {
        playerConversations.remove(player.getUniqueId());
    }

    public static void activate(Player player) {
        activePlayers.add(player.getUniqueId());
    }

    public static void deactivate(Player player) {
        activePlayers.remove(player.getUniqueId());
    }

    public static boolean isActive(Player player) {
        return activePlayers.contains(player.getUniqueId());
    }

    public static void terminate(Player player) {
        end(player);
        deactivate(player);
        setWaiting(player, false);
        clearModel(player);
    }

    public static boolean isWaiting(Player player) {
        return waitingPlayers.contains(player.getUniqueId());
    }

    public static void setWaiting(Player player, boolean waiting) {
        if (waiting) {
            waitingPlayers.add(player.getUniqueId());
        } else {
            waitingPlayers.remove(player.getUniqueId());
        }
    }

    public static void setModel(Player player, String platform, String model) {
        playerPlatform.put(player.getUniqueId(), platform);
        playerModel.put(player.getUniqueId(), model);
    }

    public static String getPlatform(Player player) {
        return playerPlatform.getOrDefault(player.getUniqueId(), "gpt");
    }

    public static String getModel(Player player) {
        return playerModel.getOrDefault(player.getUniqueId(), "gpt-4o");
    }

    public static void clearModel(Player player) {
        playerPlatform.remove(player.getUniqueId());
        playerModel.remove(player.getUniqueId());
    }

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
