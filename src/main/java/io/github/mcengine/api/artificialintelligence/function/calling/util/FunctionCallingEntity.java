package io.github.mcengine.api.artificialintelligence.function.calling.util;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Utility class for retrieving information about nearby entities around a player.
 * <p>
 * This class supports two types of retrieval:
 * - Detailed list of all nearby entities.
 * - Count of nearby entities of a specific type.
 */
public class FunctionCallingEntity {

    /**
     * Returns a detailed string listing all nearby entities around the given player within the specified radius.
     * Each line includes the entity type and its location relative to the player.
     *
     * @param plugin The plugin instance used to run main-thread safe tasks.
     * @param player The player whose surroundings are being scanned.
     * @param radius The radius to search for nearby entities.
     * @return A multiline string with entity type and relative distance info.
     */
    public static String getNearbyEntities(Plugin plugin, Player player, int radius) {
        if (Bukkit.isPrimaryThread()) {
            return getNearbyEntitiesSync(player, radius);
        } else {
            AtomicReference<String> result = new AtomicReference<>("");
            CountDownLatch latch = new CountDownLatch(1);

            Bukkit.getScheduler().runTask(plugin, () -> {
                result.set(getNearbyEntitiesSync(player, radius));
                latch.countDown();
            });

            try {
                latch.await();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            return result.get();
        }
    }

    /**
     * Returns the count of nearby entities of a given type around a player.
     * If the entity type is invalid or not supported in this server version, a warning is returned.
     * If the placeholder is used as "..._detail", a detailed string is returned instead of count.
     *
     * @param plugin     The plugin instance used to run main-thread safe tasks.
     * @param player     The player to use as the center for scanning.
     * @param entityType The entity type to filter (e.g., "ZOMBIE", "PIG").
     * @param radius     The radius to scan for nearby entities.
     * @return The count or a multiline detail string, or a warning if type is invalid.
     */
    public static String getNearbyEntities(Plugin plugin, Player player, String entityType, int radius) {
        if (Bukkit.isPrimaryThread()) {
            return getNearbyEntitiesSync(player, entityType, radius);
        } else {
            AtomicReference<String> result = new AtomicReference<>("");
            CountDownLatch latch = new CountDownLatch(1);

            Bukkit.getScheduler().runTask(plugin, () -> {
                result.set(getNearbyEntitiesSync(player, entityType, radius));
                latch.countDown();
            });

            try {
                latch.await();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            return result.get();
        }
    }

    /**
     * Synchronously retrieves a list of all nearby entities with descriptions.
     *
     * @param player The player to scan around.
     * @param radius The radius to scan.
     * @return A multiline string listing all nearby entities with distance.
     */
    private static String getNearbyEntitiesSync(Player player, int radius) {
        List<Entity> nearbyEntities = player.getNearbyEntities(radius, radius, radius);

        if (nearbyEntities.isEmpty()) {
            return "No nearby entities found.";
        }

        StringBuilder sb = new StringBuilder("Nearby entities:\n");
        for (Entity entity : nearbyEntities) {
            String type = entity.getType().name();
            double distance = entity.getLocation().distance(player.getLocation());
            sb.append("- ").append(type).append(" (").append(String.format("%.1f", distance)).append(" blocks away)\n");
        }

        return sb.toString().trim();
    }

    /**
     * Synchronously counts entities of a specific type near a player, or lists their details if entityType ends with "_detail".
     *
     * @param player     The player to scan around.
     * @param entityType The string name of the entity type.
     * @param radius     The radius to scan.
     * @return The count as a string, or detail string, or error message if type is not valid.
     */
    private static String getNearbyEntitiesSync(Player player, String entityType, int radius) {
        // Support detail: if the placeholder is used as "{nearby_<type>_detail}"
        boolean detail = entityType.endsWith("_detail");
        String baseType = detail ? entityType.substring(0, entityType.length() - "_detail".length()) : entityType;

        EntityType type;
        try {
            type = EntityType.valueOf(baseType.toUpperCase());
        } catch (IllegalArgumentException e) {
            return "This entity type isn't supported.";
        }

        List<Entity> nearbyEntities = player.getNearbyEntities(radius, radius, radius);
        List<Entity> filtered = new java.util.ArrayList<>();
        for (Entity e : nearbyEntities) {
            if (e.getType() == type) filtered.add(e);
        }

        if (detail) {
            if (filtered.isEmpty()) return "No nearby " + baseType.replace('_', ' ') + "s found.";
            StringBuilder sb = new StringBuilder("Nearby " + baseType.replace('_', ' ') + "s:\n");
            for (Entity entity : filtered) {
                double distance = entity.getLocation().distance(player.getLocation());
                sb.append("- ").append(entity.getType().name()).append(" (").append(String.format("%.1f", distance)).append(" blocks away)\n");
            }
            return sb.toString().trim();
        } else {
            return String.valueOf(filtered.size());
        }
    }
}
