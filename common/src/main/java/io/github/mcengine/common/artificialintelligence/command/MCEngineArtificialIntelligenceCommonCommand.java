package io.github.mcengine.common.artificialintelligence.command;

import io.github.mcengine.api.artificialintelligence.MCEngineArtificialIntelligenceApi;
import io.github.mcengine.api.artificialintelligence.database.IMCEngineArtificialIntelligenceApiDatabase;
import io.github.mcengine.api.artificialintelligence.util.MCEngineArtificialIntelligenceApiUtilAi;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Command executor for AI-related operations.
 * <p>
 * Supported commands:
 * - /ai set token {platform} <token>
 * - /ai get model list
 * - /ai get platform list
 */
public class MCEngineArtificialIntelligenceCommonCommand implements CommandExecutor {

    /**
     * Database instance used to persist and retrieve player tokens.
     */
    private final IMCEngineArtificialIntelligenceApiDatabase db;

    /**
     * Constructs the command executor using the provided API instance.
     *
     * @param api The MCEngineArtificialIntelligenceApi instance.
     */
    public MCEngineArtificialIntelligenceCommonCommand(MCEngineArtificialIntelligenceApi api) {
        this.db = api.getDB();
    }

    /**
     * Handles command execution for /ai commands.
     *
     * @param sender  The sender of the command.
     * @param command The command that was executed.
     * @param label   The command alias used.
     * @param args    The command arguments.
     * @return true if the command was handled successfully, false otherwise.
     */
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cOnly players can execute this command.");
            return true;
        }

        Player player = (Player) sender;

        // /ai set token {platform} <token>
        if (args.length == 4 && "set".equalsIgnoreCase(args[0]) && "token".equalsIgnoreCase(args[1])) {
            String platform = args[2];
            String token = args[3];
            db.setPlayerToken(player.getUniqueId().toString(), platform, token);
            player.sendMessage("§aSuccessfully set your token for platform: " + platform);
            return true;
        }

        // /ai get model list
        if (args.length == 3 &&
            "get".equalsIgnoreCase(args[0]) &&
            "model".equalsIgnoreCase(args[1]) &&
            "list".equalsIgnoreCase(args[2])) {

            Map<String, Map<String, ?>> models = MCEngineArtificialIntelligenceApiUtilAi.getAllModels();
            if (models.isEmpty()) {
                player.sendMessage("§cNo models are currently registered.");
                return true;
            }

            player.sendMessage("§eRegistered AI Models:");
            for (Map.Entry<String, Map<String, ?>> entry : models.entrySet()) {
                player.sendMessage("§7Platform: §b" + entry.getKey());
                if ("customurl".equalsIgnoreCase(entry.getKey())) {
                    for (String serverAndModel : entry.getValue().keySet()) {
                        String[] parts = serverAndModel.split(":", 2);
                        if (parts.length == 2) {
                            player.sendMessage("  §7- " + parts[0]);
                            player.sendMessage("    §8- " + parts[1]);
                        } else {
                            player.sendMessage("  §8- " + serverAndModel);
                        }
                    }
                } else {
                    for (String modelName : entry.getValue().keySet()) {
                        player.sendMessage("  §8- " + modelName);
                    }
                }
            }
            return true;
        }

        // /ai get platform list
        if (args.length == 3 &&
        "get".equalsIgnoreCase(args[0]) &&
        "platform".equalsIgnoreCase(args[1]) &&
        "list".equalsIgnoreCase(args[2])) {

        Map<String, Map<String, ?>> models = MCEngineArtificialIntelligenceApiUtilAi.getAllModels();
        if (models.isEmpty()) {
            player.sendMessage("§cNo platforms are currently registered.");
            return true;
        }

        player.sendMessage("§eRegistered Platforms:");
        for (Map.Entry<String, Map<String, ?>> entry : models.entrySet()) {
            String platform = entry.getKey();

            // Create clickable platform name
            TextComponent platformComponent = new TextComponent("§7- ");
            TextComponent clickablePlatform = new TextComponent("§b" + platform);
            clickablePlatform.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND,
                    "/ai set token " + platform + " "));

            platformComponent.addExtra(clickablePlatform);
            player.spigot().sendMessage(platformComponent);

            if ("customurl".equalsIgnoreCase(platform)) {
                Set<String> servers = new HashSet<>();
                for (String key : entry.getValue().keySet()) {
                    String[] parts = key.split(":", 2);
                    if (parts.length >= 1) {
                        servers.add(parts[0]);
                    }
                }

                for (String server : servers) {
                    TextComponent serverComponent = new TextComponent("   §8- ");
                    TextComponent clickableServer = new TextComponent(server);
                    clickableServer.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND,
                            "/ai set token customurl:" + server + " "));

                    serverComponent.addExtra(clickableServer);
                    player.spigot().sendMessage(serverComponent);
                }
            }
        }
        return true;
        }

        // Fallback usage
        sender.sendMessage("§cUsage:");
        sender.sendMessage("§7/ai set token {platform} <token>");
        sender.sendMessage("§7/ai get model list");
        sender.sendMessage("§7/ai get platform list");
        return true;
    }
}
