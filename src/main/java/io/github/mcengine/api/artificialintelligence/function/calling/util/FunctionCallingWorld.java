package io.github.mcengine.api.artificialintelligence.function.calling.util;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;

import java.util.concurrent.CountDownLatch;

/**
 * Utility class for thread-safe access to world-related information such as
 * entity counts and chunk data. Required because some Bukkit methods must
 * be called on the main server thread.
 */
public class FunctionCallingWorld {

    /**
     * Retrieves the number of loaded entities in the specified world in a thread-safe way.
     * <p>
     * If called asynchronously, this method schedules a synchronous task to safely
     * access the Bukkit API and blocks until the value is retrieved.
     *
     * @param plugin The plugin instance used to schedule the synchronous task.
     * @param world  The world from which to retrieve the entity count.
     * @return The number of entities currently loaded in the world, as a string.
     */
    public static String getSafeEntityCount(Plugin plugin, World world) {
        if (Bukkit.isPrimaryThread()) {
            return String.valueOf(world.getEntities().size());
        } else {
            /**
             * Stores the entity count result.
             */
            final int[] count = {0};

            /**
             * Latch to block async thread until sync task completes.
             */
            CountDownLatch latch = new CountDownLatch(1);

            // Schedule a sync task to safely call Bukkit API
            Bukkit.getScheduler().runTask(plugin, () -> {
                count[0] = world.getEntities().size();
                latch.countDown();
            });

            try {
                latch.await(); // Block current thread until sync task completes
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            return String.valueOf(count[0]);
        }
    }
}
