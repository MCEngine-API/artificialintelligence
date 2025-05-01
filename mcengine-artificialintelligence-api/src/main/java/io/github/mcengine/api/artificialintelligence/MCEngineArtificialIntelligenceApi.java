package io.github.mcengine.api.artificialintelligence;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.net.URLClassLoader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Logger;

import io.github.mcengine.api.artificialintelligence.model.*;

public class MCEngineArtificialIntelligenceApi {

    private final Plugin plugin;
    private final Logger logger;
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

    private final IMCEngineArtificialIntelligenceApiModel ai;

    public MCEngineArtificialIntelligenceApi(Plugin plugin) {
        // Set up
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        loadAddons();
        loadDLCs();

        // Load AI model
        String aiType = plugin.getConfig().getString("ai.type", "deepseek");
        switch (aiType.toLowerCase()) {
            case "custom" -> this.ai = new MCEngineArtificialIntelligenceApiModelCustomUrl(plugin);
            case "deepseek" -> this.ai = new MCEngineArtificialIntelligenceApiModelDeepSeek(plugin);
            case "openai" -> this.ai = new MCEngineArtificialIntelligenceApiModelOpenAi(plugin);
            case "openrouter" -> this.ai = new MCEngineArtificialIntelligenceApiModelOpenRouter(plugin);
            default -> throw new IllegalArgumentException("Unsupported AI type: " + aiType);
        }
    }

    public Plugin getPlugin() {
        return plugin;
    }

    // ---------------- ADDON LOADER ----------------

    private void loadAddons() {
        loadExtensions("addons", "AddOn");
    }

    // ---------------- DLC LOADER ----------------

    private void loadDLCs() {
        loadExtensions("dlcs", "DLC");
    }

    // ---------------- SHARED EXTENSION LOADER ----------------

    private void loadExtensions(String folderName, String type) {
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

        for (File file : files) {
            try (
                URLClassLoader classLoader = new URLClassLoader(
                    new URL[]{file.toURI().toURL()},
                    this.getClass().getClassLoader()
                );
                JarFile jar = new JarFile(file)
            ) {
                Enumeration<JarEntry> entries = jar.entries();

                while (entries.hasMoreElements()) {
                    JarEntry entry = entries.nextElement();
                    String name = entry.getName();

                    if (!name.endsWith(".class") || name.contains("$")) {
                        continue;
                    }

                    String className = name.replace("/", ".").replace(".class", "");

                    try {
                        Class<?> clazz = classLoader.loadClass(className);

                        if (clazz.isInterface() || Modifier.isAbstract(clazz.getModifiers())) {
                            continue;
                        }

                        Method onLoadMethod;
                        try {
                            onLoadMethod = clazz.getMethod("onLoad", MCEngineArtificialIntelligenceApi.class);
                        } catch (NoSuchMethodException e) {
                            continue;
                        }

                        Object extensionInstance = clazz.getDeclaredConstructor().newInstance();
                        onLoadMethod.invoke(extensionInstance, this);

                        logger.info("[" + type + "] Loaded: " + className);

                        registerExtensionClasses(classLoader, clazz.getPackageName(), type);

                    } catch (Throwable e) {
                        logger.warning("[" + type + "] Failed to load class: " + className);
                        e.printStackTrace();
                    }
                }
            } catch (Exception e) {
                logger.warning("[" + type + "] Error loading " + type + " JAR: " + file.getName());
                e.printStackTrace();
            }
        }
    }

    // ---------------- CLASS REGISTERING ----------------

    private void registerExtensionClasses(ClassLoader classLoader, String basePackage, String type) {
        try {
            List<Class<?>> classes = findAllClasses(classLoader, basePackage);

            for (Class<?> clazz : classes) {
                if (Listener.class.isAssignableFrom(clazz)) {
                    Listener listener;
                    try {
                        listener = (Listener) clazz.getDeclaredConstructor(MCEngineArtificialIntelligenceApi.class).newInstance(this);
                    } catch (NoSuchMethodException e) {
                        listener = (Listener) clazz.getDeclaredConstructor().newInstance();
                    }
                    registerListener(listener);
                    logger.info("[" + type + "] Registered Listener: " + clazz.getSimpleName());
                }

                if (Command.class.isAssignableFrom(clazz)) {
                    Command command;
                    try {
                        command = (Command) clazz.getDeclaredConstructor(MCEngineArtificialIntelligenceApi.class).newInstance(this);
                    } catch (NoSuchMethodException e) {
                        command = (Command) clazz.getDeclaredConstructor().newInstance();
                    }
                    registerCommand(command);
                    logger.info("[" + type + "] Registered Command: " + command.getName());
                }
            }

        } catch (Exception e) {
            logger.warning("[" + type + "] Failed to register " + type + " classes.");
            e.printStackTrace();
        }
    }

    private List<Class<?>> findAllClasses(ClassLoader classLoader, String basePackage) {
        List<Class<?>> classes = new ArrayList<>();
        String basePath = basePackage.replace(".", "/");

        try {
            URL[] urls = ((URLClassLoader) classLoader).getURLs();
            for (URL url : urls) {
                try (JarFile jarFile = new JarFile(new File(url.toURI()))) {
                    Enumeration<JarEntry> entries = jarFile.entries();
                    while (entries.hasMoreElements()) {
                        JarEntry entry = entries.nextElement();
                        String name = entry.getName();

                        if (name.endsWith(".class") && !name.contains("$") && name.startsWith(basePath)) {
                            String className = name.replace("/", ".").replace(".class", "");
                            try {
                                Class<?> clazz = classLoader.loadClass(className);
                                classes.add(clazz);
                            } catch (Throwable ignored) {}
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.warning("Failed to scan classes from JAR.");
            e.printStackTrace();
        }

        return classes;
    }

    private void registerCommand(Command command) {
        try {
            Object commandMap = Bukkit.getServer().getClass()
                .getMethod("getCommandMap")
                .invoke(Bukkit.getServer());
            commandMap.getClass()
                .getMethod("register", String.class, Command.class)
                .invoke(commandMap, command.getName(), command);
        } catch (Exception e) {
            logger.warning("Failed to register command: " + command.getName());
            e.printStackTrace();
        }
    }

    private void registerListener(Listener listener) {
        try {
            Bukkit.getPluginManager().registerEvents(listener, plugin);
        } catch (Exception e) {
            logger.warning("Failed to register listener: " + listener.getClass().getSimpleName());
            e.printStackTrace();
        }
    }

    // ---------------- TOKEN SYSTEM ----------------

    private static Date parseExpirationDate(Object input) throws ParseException {
        if (input instanceof Date) {
            return (Date) input;
        } else if (input instanceof String) {
            return DATE_FORMAT.parse((String) input);
        } else {
            throw new IllegalArgumentException("Unsupported expiration date input: " + input);
        }
    }

    public static boolean validateToken(String pluginName, String secretKey, String token, Object nowDateInput) {
        try {
            Date nowDate = parseExpirationDate(nowDateInput);

            byte[] decodedBytes = Base64.getUrlDecoder().decode(token);
            String decodedPayload = new String(decodedBytes);

            String[] parts = decodedPayload.split(":");
            if (parts.length != 4) {
                return false;
            }

            String tokenPluginName = parts[0];
            String tokenSecretKey = parts[1];
            String expirationDateStr = parts[2];

            if (!tokenPluginName.equals(pluginName)) {
                return false;
            }

            if (!tokenSecretKey.equals(secretKey)) {
                return false;
            }

            Date expirationDate = DATE_FORMAT.parse(expirationDateStr);

            String todayStr = DATE_FORMAT.format(nowDate);
            Date todayDate = DATE_FORMAT.parse(todayStr);

            return !todayDate.after(expirationDate);
        } catch (Exception e) {
            return false;
        }
    }

    public static Date extractExpirationDate(String pluginName, String secretKey, String token) {
        try {
            byte[] decodedBytes = Base64.getUrlDecoder().decode(token);
            String decodedPayload = new String(decodedBytes);

            String[] parts = decodedPayload.split(":");
            if (parts.length != 4) {
                return new Date(0L);
            }

            String tokenPluginName = parts[0];
            String tokenSecretKey = parts[1];
            String expirationDateStr = parts[2];

            if (!tokenPluginName.equals(pluginName) || !tokenSecretKey.equals(secretKey)) {
                return new Date(0L);
            }

            return DATE_FORMAT.parse(expirationDateStr);
        } catch (Exception e) {
            return new Date(0L);
        }
    }

    /**
     * Sends a message to the AI and returns the AI's response.
     *
     * @param message The message or prompt to send to the AI.
     * @return The response generated by the AI model.
     */
    public String getResponse(String message) {
        return ai.getResponse(message);
    }
}
