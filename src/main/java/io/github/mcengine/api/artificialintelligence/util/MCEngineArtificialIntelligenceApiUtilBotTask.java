package io.github.mcengine.api.artificialintelligence.util;

import com.google.gson.JsonObject;
import io.github.mcengine.api.artificialintelligence.MCEngineArtificialIntelligenceApi;
import io.github.mcengine.api.artificialintelligence.database.IMCEngineArtificialIntelligenceDB;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Asynchronous task that sends a player's input to an AI model and delivers the response.
 * <p>
 * This task performs:
 * <ul>
 *     <li>Checking and enforcing waiting state per player</li>
 *     <li>Server or player token resolution</li>
 *     <li>Conversation history injection and context building</li>
 *     <li>Prompt dispatch and response collection from AI</li>
 *     <li>Color-coded response back to the player</li>
 * </ul>
 */
public class MCEngineArtificialIntelligenceApiUtilBotTask extends BukkitRunnable {

    /** The plugin instance executing this task. */
    private final Plugin plugin;

    /** Reference to the main MCEngineArtificialIntelligence API instance. */
    private final MCEngineArtificialIntelligenceApi api;

    /** AI database interface for retrieving user tokens if needed. */
    private final IMCEngineArtificialIntelligenceDB db;

    /** Token usage mode: "server" or "player". */
    private final String tokenType;

    /** Player who triggered the bot interaction. */
    private final Player player;

    /** AI platform name (e.g., openai, deepseek, customurl). */
    private final String platform;

    /** AI model name. */
    private final String model;

    /** Message input sent by the player. */
    private final String message;

    /**
     * Constructs a new bot task for asynchronous AI interaction.
     *
     * @param plugin    The plugin instance.
     * @param api       The API instance.
     * @param db        The AI database interface.
     * @param tokenType The type of token to use ("server" or "player").
     * @param player    The player sending the message.
     * @param platform  The AI platform.
     * @param model     The model name under that platform.
     * @param message   The message to send.
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

    /**
     * Executes the asynchronous AI interaction task.
     */
    @Override
    public void run() {
        try {
            // Ignore new task if already waiting
            if (api.checkWaitingPlayer(player)) {
                Bukkit.getScheduler().runTask(plugin, () ->
                        player.sendMessage(ChatColor.RED + "⏳ Please wait for the AI to respond before sending another message.")
                );
                return;
            }

            // Mark player as waiting
            api.setWaiting(player, true);

            // Construct chat context history
            String fullPrompt = MCEngineArtificialIntelligenceApiUtilBotManager.get(player) + "[Player]: " + message;

            // Get response from API (depending on token type)
            JsonObject responseJson;
            if ("server".equalsIgnoreCase(tokenType)) {
                responseJson = api.getResponse(platform, model, fullPrompt);
            } else if ("player".equalsIgnoreCase(tokenType)) {
                String token = db.getPlayerToken(player.getUniqueId().toString(), platform);
                if (token == null || token.isEmpty()) {
                    throw new IllegalStateException("No token found for player.");
                }
                responseJson = api.getResponse(platform, model, token, fullPrompt);
            } else {
                throw new IllegalArgumentException("Unknown tokenType: " + tokenType);
            }

            // Extract content and token usage
            String replyContent = api.getCompletionContent(responseJson);
            int tokenUsed = api.getTotalTokenUsage(responseJson);

            // Log conversation
            String playerPrompt = "[Player]: " + message;
            String aiReply = "[Ai]: " + replyContent;

            // Deliver response to player on main thread
            Bukkit.getScheduler().runTask(plugin, () -> {
                player.sendMessage(ChatColor.YELLOW + "[ChatBot] " + ChatColor.RESET + replyContent);
                if (tokenUsed >= 0) {
                    player.sendMessage(ChatColor.GREEN + "[Tokens Used] " + ChatColor.RESET + tokenUsed);
                }

                MCEngineArtificialIntelligenceApiUtilBotManager.append(player, playerPrompt);
                MCEngineArtificialIntelligenceApiUtilBotManager.append(player, aiReply);
                api.setWaiting(player, false);
            });

        } catch (Exception e) {
            e.printStackTrace();
            Bukkit.getScheduler().runTask(plugin, () -> {
                player.sendMessage(ChatColor.DARK_RED + "[ChatBot] Unexpected error: " + e.getMessage());
                api.setWaiting(player, false);
            });
        }
    }
}
