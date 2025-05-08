package io.github.mcengine.common.artificialintelligence.command;

import io.github.mcengine.api.artificialintelligence.MCEngineArtificialIntelligenceApi;
import io.github.mcengine.api.artificialintelligence.database.IMCEngineArtificialIntelligenceApiDatabase;
import io.github.mcengine.api.artificialintelligence.util.MCEngineArtificialIntelligenceApiUtilAi;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.Collectors;

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
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cOnly players can execute this command.");
            return true;
        }

        if (args.length == 4 && args[0].equalsIgnoreCase("set") && args[1].equalsIgnoreCase("token")) {
            String platform = args[2];
            String token = args[3];
            db.setPlayerToken(player.getUniqueId().toString(), platform, token);
            player.sendMessage("§aSuccessfully set your token for platform: " + platform);
            return true;
        }

        if (args.length == 3 && args[0].equalsIgnoreCase("get") && args[1].equalsIgnoreCase("model") && args[2].equalsIgnoreCase("list")) {
            return handleModelList(player);
        }

        if (args.length == 3 && args[0].equalsIgnoreCase("get") && args[1].equalsIgnoreCase("platform") && args[2].equalsIgnoreCase("list")) {
            return handlePlatformList(player);
        }

        sendUsage(sender);
        return true;
    }

    /**
     * Handles the "/ai get model list" command.
     * Displays all registered AI models by platform.
     * Platforms and model names are filtered using the regex [0-9a-z]+.
     * Special handling ensures "customurl" is shown last and its servers are grouped.
     *
     * @param player The player executing the command.
     * @return true if the command executes successfully.
     */
    private boolean handleModelList(Player player) {
        Map<String, Map<String, ?>> models = MCEngineArtificialIntelligenceApiUtilAi.getAllModels();
        if (models.isEmpty()) {
            player.sendMessage("§cNo models are currently registered.");
            return true;
        }
    
        player.sendMessage("§eRegistered AI Models:");
    
        for (String platform : getSortedPlatformKeys(models)) {
            Map<String, ?> modelMap = models.get(platform);
            player.sendMessage("§7Platform: §b" + platform);
    
            if (platform.equalsIgnoreCase("customurl")) {
                // Group models by server
                Map<String, List<String>> serverModels = new TreeMap<>();
    
                for (String key : modelMap.keySet()) {
                    String[] parts = key.split(":", 2);
                    if (parts.length == 2 && isValidKey(parts[0]) && isValidKey(parts[1])) {
                        serverModels.computeIfAbsent(parts[0], k -> new ArrayList<>()).add(parts[1]);
                    }
                }
    
                for (Map.Entry<String, List<String>> entry : serverModels.entrySet()) {
                    player.sendMessage("  §7- " + entry.getKey());
                    for (String model : entry.getValue().stream().sorted().toList()) {
                        player.sendMessage("    §8- " + model);
                    }
                }
    
            } else {
                modelMap.keySet().stream()
                        .filter(this::isValidKey)
                        .sorted()
                        .forEach(modelName -> player.sendMessage("  §8- " + modelName));
            }
        }
        return true;
    }

    /**
     * Handles the "/ai get platform list" command.
     * Displays all available platforms, marking them clickable for quick token setup.
     * The "customurl" platform is shown last with its valid server list.
     *
     * @param player The player executing the command.
     * @return true if the command executes successfully.
     */
    private boolean handlePlatformList(Player player) {
        Map<String, Map<String, ?>> models = MCEngineArtificialIntelligenceApiUtilAi.getAllModels();
        if (models.isEmpty()) {
            player.sendMessage("§cNo platforms are currently registered.");
            return true;
        }

        player.sendMessage("§eRegistered Platforms:");
        for (String platform : getSortedPlatformKeys(models)) {
            Map<String, ?> entry = models.get(platform);
            if (platform.equalsIgnoreCase("customurl")) {
                player.sendMessage("§7- §b" + platform);

                List<String> servers = entry.keySet().stream()
                        .map(key -> key.split(":", 2)[0])
                        .filter(this::isValidKey)
                        .distinct()
                        .sorted()
                        .collect(Collectors.toList());

                for (String server : servers) {
                    TextComponent serverComponent = new TextComponent("   §8- ");
                    TextComponent clickableServer = new TextComponent(server);
                    clickableServer.setClickEvent(new ClickEvent(
                            ClickEvent.Action.SUGGEST_COMMAND,
                            "/ai set token customurl:" + server + " "
                    ));
                    serverComponent.addExtra(clickableServer);
                    player.spigot().sendMessage(serverComponent);
                }

            } else {
                TextComponent platformComponent = new TextComponent("§7- ");
                TextComponent clickablePlatform = new TextComponent("§b" + platform);
                clickablePlatform.setClickEvent(new ClickEvent(
                        ClickEvent.Action.SUGGEST_COMMAND,
                        "/ai set token " + platform + " "
                ));
                platformComponent.addExtra(clickablePlatform);
                player.spigot().sendMessage(platformComponent);
            }
        }
        return true;
    }

    /**
     * Sends usage help to the player or console.
     *
     * @param sender The command sender (player or console).
     */
    private void sendUsage(CommandSender sender) {
        sender.sendMessage("§cUsage:");
        sender.sendMessage("§7/ai set token {platform} <token>");
        sender.sendMessage("§7/ai get model list");
        sender.sendMessage("§7/ai get platform list");
    }

    /**
     * Sorts platform names in alphabetical order and ensures "customurl" appears last.
     * Filters platform names using [0-9a-z]+ unless it's "customurl".
     *
     * @param map The map of platform names to model lists.
     * @return A sorted list of valid platform names.
     */
    private List<String> getSortedPlatformKeys(Map<String, ?> map) {
        List<String> keys = map.keySet().stream()
                .filter(k -> isValidKey(k) || k.equalsIgnoreCase("customurl"))
                .sorted()
                .collect(Collectors.toList());

        if (keys.remove("customurl")) {
            keys.add("customurl");
        }

        return keys;
    }

    /**
     * Checks if the given key is a valid platform or model/server identifier.
     * A valid key matches the pattern [0-9a-z]+.
     *
     * @param key The string to validate.
     * @return true if the key is valid.
     */
    private boolean isValidKey(String key) {
        return key.matches("[0-9a-zA-Z_-]+");
    }
}
