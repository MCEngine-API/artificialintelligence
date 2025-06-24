package io.github.mcengine.api.artificialintelligence.util;

import io.github.mcengine.api.artificialintelligence.MCEngineArtificialIntelligenceApi;
import io.github.mcengine.api.artificialintelligence.database.IMCEngineArtificialIntelligenceDB;
import io.github.mcengine.api.artificialintelligence.model.IMCEngineArtificialIntelligenceApiModel;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Async task that sends player input to the AI API and sends the response back.
 * <p>
 * This task handles:
 * <ul>
 *   <li>Waiting-state management per player.</li>
 *   <li>Token selection (server or player-based).</li>
 *   <li>Prompt formatting and response dispatching.</li>
 * </ul>
 * Can be reused by any addon that uses {@link MCEngineArtificialIntelligenceApiUtilBotManager}.
 */
public class MCEngineArtificialIntelligenceApiUtilBotTask extends BukkitRunnable {

    /**
     * The plugin instance running the task.
     */
    private final Plugin plugin;

    private final MCEngineArtificialIntelligenceApi api;

    /**
     * The database interface used to retrieve player-specific tokens
     * for AI interactions when using the "player" token type.
     */
    private final IMCEngineArtificialIntelligenceDB db;

    /**
     * The token type to use, either "server" or "player".
     */
    private final String tokenType;

    /**
     * The player who initiated the request.
     */
    private final Player player;

    /**
     * The AI platform to interact with (e.g., openai, deepseek).
     */
    private final String platform;

    /**
     * The AI model name to use for the response.
     */
    private final String model;

    /**
     * The message content sent by the player.
     */
    private final String message;

    /**
     * Constructs a new bot task to interact with the AI.
     *
     * @param plugin    The plugin instance.
     * @param db        The AI database interface.
     * @param tokenType The type of token to use ("server" or "player").
     * @param player    The player in conversation.
     * @param platform  The AI platform to use.
     * @param model     The model name to use.
     * @param message   The message sent by the player.
     */
    public MCEngineArtificialIntelligenceApiUtilBotTask(
            Plugin plugin,
            MCEngineArtificialIntelligenceApi api,
            IMCEngineArtificialIntelligenceDB db,
            String tokenType,
            Player player,
            String platform,
            String model,
            String message
    ) {
        this.plugin = plugin;
        this.api = api;
        this.db = db;
        this.tokenType = tokenType;
        this.player = player;
        this.platform = platform;
        this.model = model;
        this.message = message;
    }

    @Override
    public void run() {
        try {
            // If player is already waiting, ignore new task
            if (api.checkWaitingPlayer(player)) {
                Bukkit.getScheduler().runTask(plugin, () ->
                        player.sendMessage(ChatColor.RED + "⏳ Please wait for the AI to respond before sending another message.")
                );
                return;
            }

            // Validate platform/model exists
            IMCEngineArtificialIntelligenceApiModel ai = api.getAi(platform, model);
            if (ai == null) {
                Bukkit.getScheduler().runTask(plugin, () ->
                        player.sendMessage("Invalid AI model or platform: " + platform + "/" + model)
                );
                return;
            }

            // Mark as waiting
            MCEngineArtificialIntelligenceApiUtilBotManager.setWaiting(player, true);

            String fullPrompt = MCEngineArtificialIntelligenceApiUtilBotManager.get(player) + "[Player]: " + message;
            String response;

            if ("server".equalsIgnoreCase(tokenType)) {
                response = api.getResponse(platform, model, fullPrompt);
            } else if ("player".equalsIgnoreCase(tokenType)) {
                String token = db.getPlayerToken(player.getUniqueId().toString(), platform);
                if (token == null || token.isEmpty()) {
                    throw new IllegalStateException("No token found for player.");
                }
                response = api.getResponse(platform, model, token, fullPrompt);
            } else {
                throw new IllegalArgumentException("Unknown tokenType: " + tokenType);
            }

            String playerPrompt = "[Player]: " + message;
            String aiReply = "[Ai]: " + response;

            Bukkit.getScheduler().runTask(plugin, () -> {
                player.sendMessage("§e[ChatBot]§r " + response);
                MCEngineArtificialIntelligenceApiUtilBotManager.append(player, playerPrompt);
                MCEngineArtificialIntelligenceApiUtilBotManager.append(player, aiReply);
                MCEngineArtificialIntelligenceApiUtilBotManager.setWaiting(player, false);
            });

        } catch (Exception e) {
            e.printStackTrace();
            Bukkit.getScheduler().runTask(plugin, () ->
                    player.sendMessage("§c[ChatBot] Unexpected error: " + e.getMessage())
            );
            MCEngineArtificialIntelligenceApiUtilBotManager.setWaiting(player, false);
        }
    }
}
