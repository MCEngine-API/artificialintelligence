package io.github.mcengine.common.artificialintelligence.tabcompleter;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Tab completer for AI commands.
 * Supports auto-completion for:
 * - /ai set token {platform} <token>
 * - /ai get model list
 * - /ai get platform list
 * - /ai get addon list
 * - /ai get dlc list
 */
public class MCEngineArtificialIntelligenceCommonTabCompleter implements TabCompleter {

    /**
     * Reference to the Bukkit plugin instance used to access the configuration.
     */
    private final Plugin plugin;

    /**
     * Top-level command keywords available at /ai <first>.
     * Includes: "set" and "get".
     */
    private static final List<String> FIRST = Arrays.asList("set", "get");

    /**
     * Second-level keywords after "set".
     * Used for: /ai set <second>.
     * Includes: "token".
     */
    private static final List<String> SECOND_SET = Arrays.asList("token");

    /**
     * Second-level keywords after "get".
     * Used for: /ai get <second>.
     * Includes: "model", "platform", "addon", "dlc".
     */
    private static final List<String> SECOND_GET = Arrays.asList("model", "platform", "addon", "dlc");

    /**
     * Third-level keywords for listing models.
     * Used for: /ai get model <third>.
     * Includes: "list".
     */
    private static final List<String> THIRD_GET_MODEL = Arrays.asList("list");

    /**
     * Third-level keywords for listing platforms.
     * Used for: /ai get platform <third>.
     * Includes: "list".
     */
    private static final List<String> THIRD_GET_PLATFORM = Arrays.asList("list");

    /**
     * Third-level keywords for listing addons or dlcs.
     * Includes: "list".
     */
    private static final List<String> THIRD_GET_EXTENSION = Arrays.asList("list");

    /**
     * Supported AI platform identifiers used when setting tokens.
     */
    private static final List<String> PLATFORMS = Arrays.asList("openai", "deepseek", "openrouter");

    /**
     * Constructs a new tab completer using the plugin instance for config access.
     *
     * @param plugin The Bukkit plugin instance.
     */
    public MCEngineArtificialIntelligenceCommonTabCompleter(Plugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Handles tab completion for /ai commands.
     *
     * @param sender  The source of the command.
     * @param command The command being executed.
     * @param alias   The alias used.
     * @param args    The command arguments.
     * @return A list of tab completion suggestions.
     */
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!command.getName().equalsIgnoreCase("ai")) return null;

        List<String> completions = new ArrayList<>();

        switch (args.length) {
            case 1 -> completions.addAll(FIRST);

            case 2 -> {
                if ("set".equalsIgnoreCase(args[0])) {
                    completions.addAll(SECOND_SET);
                } else if ("get".equalsIgnoreCase(args[0])) {
                    completions.addAll(SECOND_GET);
                }
            }

            case 3 -> {
                if ("set".equalsIgnoreCase(args[0]) && "token".equalsIgnoreCase(args[1])) {
                    completions.addAll(PLATFORMS);
                    completions.addAll(getCustomServers());
                } else if ("get".equalsIgnoreCase(args[0])) {
                    switch (args[1].toLowerCase()) {
                        case "model" -> completions.addAll(THIRD_GET_MODEL);
                        case "platform" -> completions.addAll(THIRD_GET_PLATFORM);
                        case "addon", "dlc" -> completions.addAll(THIRD_GET_EXTENSION);
                    }
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
     * Retrieves custom server names from the config under ai.custom.
     *
     * @return A list of "customurl:{server}" entries.
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
