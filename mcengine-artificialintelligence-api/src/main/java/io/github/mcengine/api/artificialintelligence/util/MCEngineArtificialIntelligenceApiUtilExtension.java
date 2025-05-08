package io.github.mcengine.api.artificialintelligence.util;

import org.bukkit.plugin.Plugin;

import java.io.File;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Logger;

/**
 * Utility class for loading AI AddOns or DLCs from JAR files.
 */
public class MCEngineArtificialIntelligenceApiUtilExtension {

    // Keeps track of loaded AddOn/DLC filenames per folder name
    private static final Map<String, List<String>> loadedExtensions = new HashMap<>();

    /**
     * Loads extensions (AddOns or DLCs) from the specified folder.
     * Scans JAR files for classes with an "onLoad(Plugin)" method and invokes them.
     *
     * @param plugin     The Bukkit plugin instance.
     * @param folderName The folder name (relative to the plugin data folder).
     * @param type       The extension type label (e.g., "AddOn", "DLC").
     */
    public static void loadExtensions(Plugin plugin, String folderName, String type) {
        Logger logger = plugin.getLogger();
        File folder = new File(plugin.getDataFolder(), folderName);

        if (!folder.exists() && !folder.mkdirs()) {
            logger.warning("[" + type + "] Could not create " + folderName + " directory.");
            return;
        }

        File[] files = folder.listFiles(file -> file.isFile() && file.getName().endsWith(".jar"));
        if (files == null || files.length == 0) {
            logger.info("[" + type + "] No " + folderName + " found.");
            return;
        }

        List<String> successfullyLoaded = new ArrayList<>();

        for (File file : files) {
            boolean loaded = false;

            try (
                URLClassLoader classLoader = new URLClassLoader(
                    new URL[]{file.toURI().toURL()},
                    MCEngineArtificialIntelligenceApiUtilExtension.class.getClassLoader()
                );
                JarFile jar = new JarFile(file)
            ) {
                Enumeration<JarEntry> entries = jar.entries();

                while (entries.hasMoreElements()) {
                    JarEntry entry = entries.nextElement();
                    String name = entry.getName();

                    if (!name.endsWith(".class") || name.contains("$")) continue;

                    String className = name.replace("/", ".").replace(".class", "");

                    try {
                        Class<?> clazz = classLoader.loadClass(className);

                        if (clazz.isInterface() || Modifier.isAbstract(clazz.getModifiers())) continue;

                        Method onLoadMethod;
                        try {
                            onLoadMethod = clazz.getMethod("onLoad", Plugin.class);
                        } catch (NoSuchMethodException e) {
                            continue;
                        }

                        Object extensionInstance = clazz.getDeclaredConstructor().newInstance();
                        onLoadMethod.invoke(extensionInstance, plugin);

                        logger.info("[" + type + "] Loaded: " + className);
                        loaded = true;
                        break; // stop after first valid class loaded
                    } catch (Throwable e) {
                        logger.warning("[" + type + "] Failed to load class: " + className);
                        e.printStackTrace();
                    }
                }

                if (loaded) {
                    successfullyLoaded.add(file.getName());
                }

            } catch (Exception e) {
                logger.warning("[" + type + "] Error loading " + type + " JAR: " + file.getName());
                e.printStackTrace();
            }
        }

        loadedExtensions.put(folderName, successfullyLoaded);
    }

    /**
     * Returns a list of successfully loaded extension JAR filenames from the specified folder.
     *
     * @param plugin     The plugin instance.
     * @param folderName The folder name ("addons" or "dlcs").
     * @return List of loaded .jar filenames from the given folder.
     */
    public static List<String> getLoadedExtensionFileNames(Plugin plugin, String folderName) {
        return loadedExtensions.getOrDefault(folderName, Collections.emptyList());
    }
}
