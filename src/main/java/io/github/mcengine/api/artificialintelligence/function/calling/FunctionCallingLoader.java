package io.github.mcengine.api.artificialintelligence.function.calling;

import io.github.mcengine.api.artificialintelligence.function.calling.json.FunctionCallingJson;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.*;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import static io.github.mcengine.api.artificialintelligence.function.calling.util.FunctionCallingEntity.*;
import static io.github.mcengine.api.artificialintelligence.function.calling.util.FunctionCallingItem.*;
import static io.github.mcengine.api.artificialintelligence.function.calling.util.FunctionCallingLoaderUtilTime.*;
import static io.github.mcengine.api.artificialintelligence.function.calling.util.FunctionCallingWorld.*;

/**
 * Loads and matches function-calling rules for the MCEngineChatBot plugin.
 * <p>
 * Key features:
 * <ul>
 *   <li>Placeholder replacement (player/world/time variables).</li>
 *   <li>Fuzzy matching via "words-in-order" regex (e.g., {@code .*w1.*w2.*}).</li>
 *   <li><b>Decision tree index</b> to avoid O(N) scans across all rules.</li>
 * </ul>
 */
public class FunctionCallingLoader {

    /** The main plugin instance for file lookups and context. */
    private final Plugin plugin;

    /**
     * Word-anchored decision tree of compiled match patterns.
     * <p>
     * The tree maps the <i>first token</i> of a rule's match string to a bucket of compiled patterns.
     * At query-time we tokenize the user input and only evaluate buckets whose anchor token
     * appears in the input (plus a small {@code *} fallback bucket).
     */
    private final DecisionTree merged_rules = new DecisionTree();

    /**
     * Map of placeholder keys to suppliers, kept in memory for efficient access.
     * The supplier receives the Player context at runtime.
     */
    private final Map<String, java.util.function.Function<Player, String>> placeholders = new LinkedHashMap<>();

    /**
     * Loads the map of placeholder keys to value functions into memory.
     * This method should be called once, ideally from the constructor.
     */
    private void loadPlaceholder() {
        // --- Entity Placeholder Map ---
        placeholders.put("{nearby_entities_count}", player -> getNearbyEntities(plugin, player, 20));
        placeholders.put("{nearby_entities_detail}", player -> getNearbyEntities(plugin, player, 20));
        String[] entityTypes = {
            "allay", "armadillo", "axolotl", "bat", "bee", "blaze", "bogged", "breeze",
            "camel", "cat", "cave_spider", "chicken", "cod", "cow", "creeper", "dolphin",
            "donkey", "drowned", "elder_guardian", "ender_dragon", "endermite", "evoker",
            "fox", "frog", "ghast", "glow_squid", "goat", "guardian", "hoglin", "horse",
            "husk", "illusioner", "iron_golem", "llama", "magma_cube", "mooshroom", "mule",
            "ocelot", "panda", "parrot", "phantom", "pig", "piglin", "piglin_brute",
            "pillager", "polar_bear", "pufferfish", "rabbit", "ravager", "salmon", "sheep",
            "shulker", "silverfish", "skeleton", "skeleton_horse", "slime", "sniffer",
            "snow_golem", "spider", "squid", "stray", "strider", "trader_llama",
            "tropical_fish", "turtle", "vex", "vindicator", "warden", "witch", "wither",
            "wither_skeleton", "wolf", "zoglin", "zombie", "zombie_horse", "zombie_villager",
            "zombified_piglin"
        };
        for (String type : entityTypes) {
            placeholders.put("{nearby_" + type + "_count}", player -> getNearbyEntities(plugin, player, type, 20));
            placeholders.put("{nearby_" + type + "_detail}", player -> getNearbyEntities(plugin, player, type, 20));
        }

        // --- Player-related placeholders (sorted) ---
        placeholders.put("{item_in_hand}", player -> getItemInHandDetails(player));
        placeholders.put("{player_displayname}", Player::getDisplayName);
        placeholders.put("{player_exp_level}", player -> String.valueOf(player.getLevel()));
        placeholders.put("{player_food_level}", player -> String.valueOf(player.getFoodLevel()));
        placeholders.put("{player_gamemode}", player -> player.getGameMode().name());
        placeholders.put("{player_health}", player -> String.valueOf(player.getHealth()));
        placeholders.put("{player_inventory}", player -> getPlayerInventoryDetails(player));
        placeholders.put("{player_ip}", player -> player.getAddress() != null ? player.getAddress().getAddress().getHostAddress() : "unknown");
        placeholders.put("{player_location}", player -> String.format("X: %.1f, Y: %.1f, Z: %.1f",
                player.getLocation().getX(), player.getLocation().getY(), player.getLocation().getZ()));
        placeholders.put("{player_max_health}", player -> String.valueOf(player.getMaxHealth()));
        placeholders.put("{player_name}", Player::getName);
        placeholders.put("{player_uuid}", player -> player.getUniqueId().toString());
        placeholders.put("{player_uuid_short}", player -> player.getUniqueId().toString().split("-")[0]);
        placeholders.put("{player_world}", player -> player.getWorld().getName());

        // --- World and environment placeholders (sorted) ---
        placeholders.put("{world_difficulty}", player -> player.getWorld().getDifficulty().name());
        placeholders.put("{world_entity_count}", player -> getSafeEntityCount(plugin, player.getWorld()));
        placeholders.put("{world_loaded_chunks}", player -> String.valueOf(player.getWorld().getLoadedChunks().length));
        placeholders.put("{world_seed}", player -> String.valueOf(player.getWorld().getSeed()));
        placeholders.put("{world_time}", player -> String.valueOf(player.getWorld().getTime()));
        placeholders.put("{world_weather}", player -> player.getWorld().hasStorm() ? "Raining" : "Clear");

        // --- Static time zones ---
        placeholders.put("{time_gmt}", player -> getFormattedTime(TimeZone.getTimeZone("GMT")));
        placeholders.put("{time_server}", player -> getFormattedTime(TimeZone.getDefault()));
        placeholders.put("{time_utc}", player -> getFormattedTime(TimeZone.getTimeZone("UTC")));

        // --- Named zones ---
        Map<String, String> namedZones = Map.ofEntries(
                Map.entry("{time_bangkok}", getFormattedTime("Asia/Bangkok")),
                Map.entry("{time_berlin}", getFormattedTime("Europe/Berlin")),
                Map.entry("{time_london}", getFormattedTime("Europe/London")),
                Map.entry("{time_los_angeles}", getFormattedTime("America/Los_Angeles")),
                Map.entry("{time_new_york}", getFormattedTime("America/New_York")),
                Map.entry("{time_paris}", getFormattedTime("Europe/Paris")),
                Map.entry("{time_singapore}", getFormattedTime("Asia/Singapore")),
                Map.entry("{time_sydney}", getFormattedTime("Australia/Sydney")),
                Map.entry("{time_tokyo}", getFormattedTime("Asia/Tokyo")),
                Map.entry("{time_toronto}", getFormattedTime("America/Toronto"))
        );
        for (Map.Entry<String, String> entry : namedZones.entrySet()) {
            placeholders.put(entry.getKey(), player -> entry.getValue());
        }
    }

    /**
     * Constructs the loader and builds the decision tree index from all rules in the configured directory.
     * Logs the number of rules loaded and initializes the placeholder map.
     *
     * @param plugin     The plugin instance used for locating the data folder.
     * @param folderPath The folder path relative to the plugin data directory.
     * @param logger     The logger instance used for logging info to console.
     */
    public FunctionCallingLoader(Plugin plugin, String folderPath, Logger logger) {
        this.plugin = plugin;

        IFunctionCallingLoader loader = new FunctionCallingJson(
                new java.io.File(plugin.getDataFolder(), folderPath)
        );

        // Build the indexed decision tree
        int ruleCount = 0;
        for (FunctionRule rule : loader.loadFunctionRules()) {
            ruleCount++;
            for (String raw : rule.getMatch()) {
                final String pattern = convertToRegex(raw);
                final Pattern compiled = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);

                final String firstToken = firstToken(raw);
                merged_rules.insert(firstToken, new PatternEntry(compiled, rule.getResponse()));
            }
        }

        loadPlaceholder();
        logger.info("Loaded " + ruleCount + " function rules; indexed into " + merged_rules.bucketCount() + " buckets.");
    }

    /**
     * Verifies this class is loaded and outputs a test log message.
     *
     * @param logger The logger to use for output.
     */
    public static void check(Logger logger) {
        logger.info("Class: FunctionCallingLoader is loaded.");
    }

    /**
     * Matches player input against indexed rule buckets, then validates candidates with compiled regex.
     * If any rule matches, the response will be dynamically filled with placeholders and returned.
     *
     * @param player The player who sent the input.
     * @param input  The raw user input text.
     * @return A list of response strings that matched and were resolved with placeholders.
     */
    public List<String> match(Player player, String input) {
        List<String> results = new ArrayList<>();
        final String trimmedInput = input.trim();
        if (trimmedInput.isEmpty()) return results;

        // Tokenize input once (lowercased) to pick buckets.
        final Set<String> inputTokens = tokenize(trimmedInput);

        // Evaluate pattern buckets that share at least one anchor with the input.
        final List<PatternEntry> candidates = merged_rules.candidatesFor(inputTokens);

        for (PatternEntry entry : candidates) {
            if (entry.pattern.matcher(trimmedInput).find()) {
                final String resolved = applyPlaceholders(entry.response, player);
                results.add(resolved);
            }
        }
        return results;
    }

    /**
     * Converts a plain user-friendly match string into a basic regex pattern for fuzzy matching.
     *
     * @param text The plain text pattern from JSON.
     * @return A regex pattern string.
     */
    private String convertToRegex(String text) {
        String[] words = text.trim().toLowerCase().split("\\s+");
        return ".*" + String.join(".*", words) + ".*";
    }

    /**
     * Replaces placeholders in a chatbot response with real-time values from the player or server.
     * Uses cached placeholder function map for optimal performance.
     *
     * @param response The raw response containing placeholders.
     * @param player   The player whose data is used for substitution.
     * @return A fully resolved string with all placeholders replaced.
     */
    private String applyPlaceholders(String response, Player player) {
        String result = response;

        for (Map.Entry<String, java.util.function.Function<Player, String>> entry : placeholders.entrySet()) {
            result = result.replace(entry.getKey(), entry.getValue().apply(player));
        }

        for (int hour = -12; hour <= 14; hour++) {
            for (int min : new int[]{0, 30, 45}) {
                String utcLabel = getZoneLabel("utc", hour, min);
                String gmtLabel = getZoneLabel("gmt", hour, min);
                TimeZone tz = TimeZone.getTimeZone(String.format("GMT%+03d:%02d", hour, min));
                String time = getFormattedTime(tz);
                result = result.replace(utcLabel, time);
                result = result.replace(gmtLabel, time);
            }
        }

        return result;
    }

    // ------------------------------
    // Helpers: tokenization & anchor
    // ------------------------------

    /**
     * Returns the first lowercase token in a match string; {@code "*"} if none.
     */
    private static String firstToken(String raw) {
        String[] parts = raw == null ? new String[0] : raw.trim().toLowerCase().split("\\s+");
        return parts.length > 0 ? parts[0] : DecisionTree.FALLBACK_KEY;
    }

    /**
     * Tokenizes arbitrary input into a set of lowercase words.
     */
    private static Set<String> tokenize(String input) {
        String[] parts = input.toLowerCase().split("\\s+");
        return new LinkedHashSet<>(Arrays.asList(parts));
    }

    // ------------------------------
    // Decision tree implementation
    // ------------------------------

    /**
     * Immutable data object for a compiled candidate pattern and its associated response template.
     */
    private static final class PatternEntry {
        /** Pre-compiled fuzzy regex (e.g., {@code .*w1.*w2.*}). */
        private final Pattern pattern;
        /** Response template to render if the pattern matches. */
        private final String response;

        private PatternEntry(Pattern pattern, String response) {
            this.pattern = pattern;
            this.response = response;
        }
    }

    /**
     * A lightweight word-anchored decision tree.
     * <p>
     * The root maintains buckets keyed by the first token of each rule's match string.
     * A special {@link #FALLBACK_KEY} bucket captures empty/degenerate cases.
     */
    private static final class DecisionTree {
        /** Key used for rules without a usable first token. */
        private static final String FALLBACK_KEY = "*";

        /** Root buckets: anchor token → list of candidate patterns. */
        private final Map<String, List<PatternEntry>> root = new HashMap<>();

        /**
         * Inserts a compiled pattern under the given anchor token.
         *
         * @param anchor first token (lowercased) or {@link #FALLBACK_KEY}
         * @param entry  compiled pattern + response
         */
        private void insert(String anchor, PatternEntry entry) {
            root.computeIfAbsent(anchor == null || anchor.isEmpty() ? FALLBACK_KEY : anchor,
                    k -> new ArrayList<>()).add(entry);
        }

        /**
         * Returns all candidate patterns whose anchor tokens appear in the provided input tokens,
         * plus a small fallback bucket.
         *
         * @param inputTokens lowercased tokens from the user's input
         * @return ordered list of candidate patterns to test
         */
        private List<PatternEntry> candidatesFor(Set<String> inputTokens) {
            // Preserve insertion order deterministically using LinkedHashSet.
            LinkedHashSet<PatternEntry> out = new LinkedHashSet<>();

            for (String token : inputTokens) {
                List<PatternEntry> bucket = root.get(token);
                if (bucket != null) out.addAll(bucket);
            }
            // Always check the fallback bucket last.
            List<PatternEntry> star = root.get(FALLBACK_KEY);
            if (star != null) out.addAll(star);

            return new ArrayList<>(out);
        }

        /**
         * @return number of non-empty buckets in the root map.
         */
        private int bucketCount() {
            return root.size();
        }
    }
}
