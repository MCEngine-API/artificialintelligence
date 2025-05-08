package io.github.mcengine.common.artificialintelligence.tabcompleter;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MCEngineArtificialIntelligenceCommonTabCompleter implements TabCompleter {

    private final Plugin plugin;

    private static final List<String> FIRST = Arrays.asList("set");
    private static final List<String> SECOND = Arrays.asList("token");
    private static final List<String> PLATFORMS = Arrays.asList("openai", "deepseek", "openrouter");

    public MCEngineArtificialIntelligenceCommonTabCompleter(Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!command.getName().equalsIgnoreCase("ai")) return null;

        List<String> completions = new ArrayList<>();

        switch (args.length) {
            case 1 -> completions.addAll(FIRST);
            case 2 -> {
                if ("set".equalsIgnoreCase(args[0])) {
                    completions.addAll(SECOND);
                }
            }
            case 3 -> {
                if ("set".equalsIgnoreCase(args[0]) && "token".equalsIgnoreCase(args[1])) {
                    completions.addAll(PLATFORMS);
                    completions.addAll(getCustomServers());
                }
            }
            case 4 -> {
                if ("set".equalsIgnoreCase(args[0]) && "token".equalsIgnoreCase(args[1])) {
                    completions.add("<your_token>");
                }
            }
        }

        return completions;
    }

    /**
     * Retrieves all keys from config section ai.custom and returns them as "customurl:{server}".
     */
    private List<String> getCustomServers() {
        List<String> custom = new ArrayList<>();
        ConfigurationSection section = plugin.getConfig().getConfigurationSection("ai.custom");
        if (section != null) {
            for (String key : section.getKeys(false)) {
                custom.add("customurl:" + key);
            }
        }
        return custom;
    }
}
