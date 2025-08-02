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
     * Writes a default `data.json` file with two example FunctionRule entries.
     * @param file the file to write to
     */
    private void writeDefaultDataJson(File file) {
        try (FileWriter writer = new FileWriter(file)) {
            List<FunctionRule> defaultRules = Arrays.asList(
                    new FunctionRule(
                            Arrays.asList("What game am I playing right now?", "Which game am I currently playing?"),
                            "You are playing Minecraft right now."
                    ),
                    new FunctionRule(
                            Arrays.asList("What plugin is this?", "Which plugin is running?"),
                            "This plugin is MCEngine Artificial Intelligence."
                    )
            );

            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            gson.toJson(defaultRules, writer);
            System.out.println("✅ Created default function rules at: " + file.getPath());

        } catch (IOException e) {
            System.err.println("⚠️ Could not write default data.json to: " + file.getPath());
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
