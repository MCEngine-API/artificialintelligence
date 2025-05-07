package io.github.mcengine.common.artificialintelligence.command;

import io.github.mcengine.api.artificialintelligence.MCEngineArtificialIntelligenceApi;
import io.github.mcengine.api.artificialintelligence.database.IMCEngineArtificialIntelligenceApiDatabase;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Command executor for managing AI tokens using the format "/ai {platform} token set {token}".
 */
public class MCEngineArtificialIntelligenceCommonCommand implements CommandExecutor {

    private final IMCEngineArtificialIntelligenceApiDatabase db;

    /**
     * Constructs a new command executor with database access from the API.
     *
     * @param api The MCEngineArtificialIntelligenceApi instance.
     */
    public MCEngineArtificialIntelligenceCommonCommand(MCEngineArtificialIntelligenceApi api) {
        this.db = api.getDB();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Usage: /ai {platform} token set {token}
        if (args.length != 4 || !"token".equalsIgnoreCase(args[1]) || !"set".equalsIgnoreCase(args[2])) {
            sender.sendMessage("§cUsage: /ai {platform} token set {token}");
            return true;
        }

        if (!(sender instanceof Player)) {
            sender.sendMessage("§cOnly players can execute this command.");
            return true;
        }

        Player player = (Player) sender;
        String playerUuid = player.getUniqueId().toString();
        String platform = args[0];
        String token = args[3];

        if (!player.hasPermission("mcengine.ai.token.set")) {
            sender.sendMessage("§cYou do not have permission to set tokens.");
            return true;
        }

        db.setPlayerToken(playerUuid, platform, token);
        sender.sendMessage("§aSuccessfully set your token for platform: " + platform);

        return true;
    }
}
