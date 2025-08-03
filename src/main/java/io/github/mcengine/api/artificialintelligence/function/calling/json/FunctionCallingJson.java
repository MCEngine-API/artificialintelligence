package io.github.mcengine.api.artificialintelligence.function.calling.json;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import com.google.gson.reflect.TypeToken;
import io.github.mcengine.api.artificialintelligence.function.calling.FunctionRule;
import io.github.mcengine.api.artificialintelligence.function.calling.IFunctionCallingLoader;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Loads function calling rules recursively from all `.json` files under the specified root folder.
 * - Supports multiple JSON files
 * - Supports recursive directory traversal
 * - Keeps comments in JSON using lenient parsing
 * - Writes a default `data.json` if the folder is newly created and empty
 */
public class FunctionCallingJson implements IFunctionCallingLoader {

    private final File rootFolder;

    /**
     * Constructs a FunctionCallingJson loader with the specified root folder.
     * @param rootFolder the directory to scan recursively for `.json` files
     */
    public FunctionCallingJson(File rootFolder) {
        this.rootFolder = rootFolder;
        ensureDefaultDataJsonExists();
    }

    /**
     * Loads and aggregates all FunctionRule entries from every `.json` file found under the root folder.
     * @return a list of all loaded FunctionRule objects
     */
    @Override
    public List<FunctionRule> loadFunctionRules() {
        List<FunctionRule> allRules = new ArrayList<>();
        try {
            List<File> jsonFiles = listAllJsonFiles(rootFolder);
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            Type type = new TypeToken<List<FunctionRule>>() {}.getType();

            for (File file : jsonFiles) {
                try (FileReader fr = new FileReader(file);
                     JsonReader reader = new JsonReader(fr)) {

                    reader.setLenient(true); // Allow comments and non-strict JSON
                    List<FunctionRule> rules = gson.fromJson(reader, type);
                    if (rules != null) {
                        allRules.addAll(rules);
                    }
                } catch (Exception e) {
                    System.err.println("❌ Failed to load JSON from: " + file.getPath());
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return allRules;
    }

    /**
     * Recursively lists all `.json` files under the given folder.
     * @param folder the starting directory
     * @return list of `.json` files found
     */
    private List<File> listAllJsonFiles(File folder) {
        List<File> result = new ArrayList<>();
        if (folder == null || !folder.exists()) return result;

        File[] files = folder.listFiles();
        if (files == null) return result;

        for (File file : files) {
            if (file.isDirectory()) {
                result.addAll(listAllJsonFiles(file)); // Recurse into subdirectories
            } else if (file.getName().toLowerCase().endsWith(".json")) {
                result.add(file);
            }
        }

        return result;
    }

    /**
     * Ensures a default `data.json` is written if the folder doesn't exist or is empty.
     * Will not overwrite existing files.
     */
    private void ensureDefaultDataJsonExists() {
        try {
            File defaultFile = new File(rootFolder, "data.json");

            if (!rootFolder.exists()) {
                if (rootFolder.mkdirs()) {
                    writeDefaultDataJson(defaultFile);
                }
            }

            if (rootFolder.exists() && !defaultFile.exists() && isFolderEmpty(rootFolder)) {
                writeDefaultDataJson(defaultFile);
            }

        } catch (Exception e) {
            System.err.println("⚠️ Failed to create default data.json in: " + rootFolder.getPath());
            e.printStackTrace();
        }
    }

    /**
     * Writes a default `data.json` file containing extensive example chatbot rules.
     * This method is useful for standalone execution outside the plugin lifecycle.
     *
     * @param file the destination file for writing the default data
     */
    private void writeDefaultDataJson(File file) {
        try (FileWriter writer = new FileWriter(file)) {
            List<Map<String, Object>> data = new ArrayList<>();

            // Generic nearby entities (count/detail)
            data.add(Map.of(
                "match", Arrays.asList("What mobs are near me?", "List nearby entities"),
                "response", "Nearby entities:\n{nearby_entities_count}"
            ));
            data.add(Map.of(
                "match", Arrays.asList("Show nearby entities detail", "List nearby entity details"),
                "response", "Nearby entities:\n{nearby_entities_detail}"
            ));

            // Entity-specific rules
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
                String name = type.replace('_', ' ');
                boolean endsWithS = name.endsWith("s");
                data.add(Map.of(
                    "match", Arrays.asList(
                        "How many " + name + "s nearby?",
                        "Nearby " + name + " count"
                    ),
                    "response", "There are {nearby_" + type + "_count} " + name + (endsWithS ? "" : "s") + " near you."
                ));
                data.add(Map.of(
                    "match", Arrays.asList(
                        "Show nearby " + name + " detail",
                        "Nearby " + name + " details"
                    ),
                    "response", "Nearby " + name + (endsWithS ? "" : "s") + ":\n{nearby_" + type + "_detail}"
                ));
            }

            // Item, player, world, and time-related entries
            data.add(Map.of("match", Arrays.asList("What is in my hand?", "Show my held item"), "response", "You are holding: {item_in_hand}"));
            data.add(Map.of("match", Arrays.asList("What is my display name?", "Show display name"), "response", "Your display name is {player_displayname}."));
            data.add(Map.of("match", Arrays.asList("How much XP do I have?", "What is my level?"), "response", "Your experience level is {player_exp_level}."));
            data.add(Map.of("match", Arrays.asList("How hungry am I?", "What is my food level?"), "response", "Your food level is {player_food_level}."));
            data.add(Map.of("match", Arrays.asList("What mode am I in?", "Tell me my game mode"), "response", "You are in {player_gamemode} mode."));
            data.add(Map.of("match", Arrays.asList("How much health do I have?", "Tell me my health"), "response", "You have {player_health} health."));
            data.add(Map.of("match", Arrays.asList("What is in my inventory?", "List my items"), "response", "Inventory contents:\n{player_inventory}"));
            data.add(Map.of("match", Arrays.asList("What is my IP address?", "Tell me my IP"), "response", "Your IP address is {player_ip}."));
            data.add(Map.of("match", Arrays.asList("Where am I?", "Tell me my location"), "response", "You are at {player_location} in world {player_world}."));
            data.add(Map.of("match", Arrays.asList("What is my max health?", "Max HP"), "response", "Your max health is {player_max_health}."));
            data.add(Map.of("match", Arrays.asList("What is my name?", "Who am I?"), "response", "Your name is {player_name}."));
            data.add(Map.of("match", Arrays.asList("What is my UUID?", "Tell me my player ID"), "response", "Your UUID is {player_uuid}."));
            data.add(Map.of("match", Arrays.asList("What is my short UUID?", "Shorten my UUID"), "response", "Short UUID: {player_uuid_short}"));
            data.add(Map.of("match", Arrays.asList("What world am I in?", "Tell me my world"), "response", "You are in world: {player_world}."));
            data.add(Map.of("match", Arrays.asList("How hard is this world?", "Tell me world difficulty"), "response", "World difficulty: {world_difficulty}"));
            data.add(Map.of("match", Arrays.asList("How many entities are in the world?"), "response", "Entities in world: {world_entity_count}"));
            data.add(Map.of("match", Arrays.asList("How many chunks are loaded?"), "response", "Loaded chunks: {world_loaded_chunks}"));
            data.add(Map.of("match", Arrays.asList("What is the seed?", "World seed?"), "response", "World seed: {world_seed}"));
            data.add(Map.of("match", Arrays.asList("What time is it in-game?", "Tell me Minecraft time"), "response", "World time: {world_time}"));
            data.add(Map.of("match", Arrays.asList("What is the weather like?", "Current weather?"), "response", "World weather: {world_weather}"));

            data.add(Map.of("match", Arrays.asList("What is the server time?", "Current server time"), "response", "Server time is {time_server}."));
            data.add(Map.of("match", Arrays.asList("What is the UTC time?", "Tell me UTC time"), "response", "UTC time is {time_utc}."));
            data.add(Map.of("match", Arrays.asList("What is GMT time?", "Time in GMT?"), "response", "GMT time is {time_gmt}."));
            data.add(Map.of("match", Arrays.asList("Bangkok time?", "What time is it in Bangkok?"), "response", "Bangkok time is {time_bangkok}."));
            data.add(Map.of("match", Arrays.asList("Berlin time?", "What time is it in Berlin?"), "response", "Berlin time is {time_berlin}."));
            data.add(Map.of("match", Arrays.asList("London time?", "What time is it in London?"), "response", "London time is {time_london}."));
            data.add(Map.of("match", Arrays.asList("LA time?", "What time is it in Los Angeles?"), "response", "Los Angeles time is {time_los_angeles}."));
            data.add(Map.of("match", Arrays.asList("New York time?", "What time is it in New York?"), "response", "New York time is {time_new_york}."));
            data.add(Map.of("match", Arrays.asList("Paris time?", "What time is it in Paris?"), "response", "Paris time is {time_paris}."));
            data.add(Map.of("match", Arrays.asList("Singapore time?", "What time is it in Singapore?"), "response", "Singapore time is {time_singapore}."));
            data.add(Map.of("match", Arrays.asList("Sydney time?", "What time is it in Sydney?"), "response", "Sydney time is {time_sydney}."));
            data.add(Map.of("match", Arrays.asList("Tokyo time?", "What time is it in Tokyo?"), "response", "Tokyo time is {time_tokyo}."));
            data.add(Map.of("match", Arrays.asList("Toronto time?", "What time is it in Toronto?"), "response", "Toronto time is {time_toronto}."));
            data.add(Map.of("match", Arrays.asList("What is time in GMT+7?", "Time in UTC+7?"), "response", "Time in GMT+7 is {time_gmt_plus_07_00}."));

            // Placeholders list
            data.add(Map.of(
                "match", Arrays.asList("Tell me all placeholders", "Show me the AI variables"),
                "response", "Placeholders: {player_name}, {player_uuid}, {player_displayname}, {player_ip}, {player_gamemode}, "
                    + "{player_health}, {player_max_health}, {player_food_level}, {player_exp_level}, {player_location}, "
                    + "{player_world}, {item_in_hand}, {player_inventory}, {time_server}, {time_utc}, {time_gmt}, "
                    + "{time_bangkok}, {time_berlin}, {time_london}, {time_los_angeles}, {time_new_york}, {time_paris}, "
                    + "{time_singapore}, {time_sydney}, {time_tokyo}, {time_toronto}, {time_gmt_plus_07_00}"
            ));

            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            gson.toJson(data, writer);
            System.out.println("✅ Created default data.json at: " + file.getPath());

        } catch (IOException e) {
            System.err.println("⚠️ Failed to write default data.json to: " + file.getPath());
            e.printStackTrace();
        }
    }

    /**
     * Checks whether a folder is empty (has no files or subfolders).
     * @param folder the folder to check
     * @return true if empty, false otherwise
     */
    private boolean isFolderEmpty(File folder) {
        File[] files = folder.listFiles();
        return files == null || files.length == 0;
    }
}
