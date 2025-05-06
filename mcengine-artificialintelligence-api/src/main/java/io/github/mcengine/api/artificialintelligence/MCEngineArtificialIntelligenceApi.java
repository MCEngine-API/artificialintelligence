package io.github.mcengine.api.artificialintelligence;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLClassLoader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Logger;

import io.github.mcengine.api.artificialintelligence.Metrics;
import io.github.mcengine.api.artificialintelligence.model.*;

/**
 * Main API class for MCEngineArtificialIntelligence.
 * Handles AI model initialization, extension loading (AddOns/DLCs), and token validation.
 */
public class MCEngineArtificialIntelligenceApi {

    /**
     * The GitHub token used for API authentication when checking for updates.
     */
    private final String github_token;

    /**
     * The Bukkit plugin instance associated with this AI API.
     */
    private final Plugin plugin;

    /**
     * Logger for logging plugin-related messages.
     */
    private final Logger logger;

    /**
     * Date format used for parsing and formatting expiration dates.
     */
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

    /**
     * The AI model instance for DeepSeek.
     */
    private final IMCEngineArtificialIntelligenceApiModel aiDeepSeek;

    /**
     * The AI model instance for OpenAI.
     */
    private final IMCEngineArtificialIntelligenceApiModel aiOpenAi;

    /**
     * The AI model instance for OpenRouter.
     */
    private final IMCEngineArtificialIntelligenceApiModel aiOpenRouter;

    /**
     * The AI model instance for a custom URL endpoint.
     */
    private final IMCEngineArtificialIntelligenceApiModel aiCustomUrl;

    // 🔥 GLOBAL cache shared across ALL plugins (platform → (model → instance))
    private static final Map<String, Map<String, IMCEngineArtificialIntelligenceApiModel>> modelCache = new HashMap<>();

    /**
     * Constructs a new AI API instance with the given plugin.
     * Initializes the AI model and loads addons and DLCs.
     *
     * @param plugin The Bukkit plugin instance.
     */
    public MCEngineArtificialIntelligenceApi(Plugin plugin) {
        new Metrics(plugin, 25556);
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        this.github_token = plugin.getConfig().getString("github.token", "null");
        loadAddOns();
        loadDLCs();

        // Load AI model
        this.aiDeepSeek = new MCEngineArtificialIntelligenceApiModelDeepSeek(plugin);
        this.aiOpenAi = new MCEngineArtificialIntelligenceApiModelOpenAi(plugin);
        this.aiOpenRouter = new MCEngineArtificialIntelligenceApiModelOpenRouter(plugin);
        this.aiCustomUrl = new MCEngineArtificialIntelligenceApiModelCustomUrl(plugin);
    }

    public void checkUpdate(String gitPlatform, String org, String repository) {
        switch (gitPlatform.toLowerCase()) {
            case "github":
                checkUpdateGitHub(org, repository);
                break;
            case "gitlab":
                checkUpdateGitLab(org, repository);
                break;
            default:
                logger.warning("Unknown platform: " + gitPlatform);
        }
    }

    private void checkUpdateGitHub(String org, String repository) {
        String apiUrl = String.format("https://api.github.com/repos/%s/%s/releases/latest", org, repository);
        String downloadUrl = String.format("https://github.com/%s/%s/releases", org, repository);
        fetchAndCompareUpdate(apiUrl, downloadUrl, "application/vnd.github.v3+json", false);
    }

    private void checkUpdateGitLab(String org, String repository) {
        String apiUrl = String.format("https://gitlab.com/api/v4/projects/%s%%2F%s/releases", org, repository);
        String downloadUrl = String.format("https://gitlab.com/%s/%s/-/releases", org, repository);
        fetchAndCompareUpdate(apiUrl, downloadUrl, "application/json", true);
    }

    private void fetchAndCompareUpdate(String apiUrl, String downloadUrl, String acceptHeader, boolean jsonArray) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                HttpURLConnection con = (HttpURLConnection) new URL(apiUrl).openConnection();
                con.setRequestMethod("GET");
                con.setRequestProperty("Authorization", "token " + github_token);
                con.setRequestProperty("Accept", acceptHeader);
                con.setDoOutput(true);

                JsonReader reader = new JsonReader(new InputStreamReader(con.getInputStream()));
                String latestVersion;

                if (jsonArray) {
                    // GitLab returns array
                    var jsonArrayObj = JsonParser.parseReader(reader).getAsJsonArray();
                    latestVersion = jsonArrayObj.size() > 0 ? jsonArrayObj.get(0).getAsJsonObject().get("tag_name").getAsString() : null;
                } else {
                    // GitHub returns object
                    var jsonObject = JsonParser.parseReader(reader).getAsJsonObject();
                    latestVersion = jsonObject.get("tag_name").getAsString();
                }

                if (latestVersion == null) {
                    logger.warning("Could not find release tag from API: " + apiUrl);
                    return;
                }

                String version = plugin.getDescription().getVersion();
                boolean changed = isUpdateAvailable(version, latestVersion);

                if (changed) {
                    List<String> updateMessages = new ArrayList<>();
                    updateMessages.add("§9[MCEngineArtificialIntelligence]§r §6A new update is available!");
                    updateMessages.add("§9[MCEngineArtificialIntelligence]§r Current version: §e" + version + " §r>> Latest: §a" + latestVersion);
                    updateMessages.add("§9[MCEngineArtificialIntelligence]§r Download: §b" + downloadUrl);

                    updateMessages.forEach(msg -> Bukkit.getConsoleSender().sendMessage(msg));
                } else {
                    logger.info("No updates found. You are running the latest version.");
                }
            } catch (Exception ex) {
                logger.warning("Could not check for updates from " + apiUrl + ": " + ex.getMessage());
            }
        });
    }

    private boolean isUpdateAvailable(String currentVersion, String latestVersion) {
        String[] lv = latestVersion.split("\\.");
        String[] cv = currentVersion.split("\\.");

        boolean changed = lv.length != cv.length;
        changed = changed || !lv[0].equals(cv[0]); // Major
        if (!changed && lv.length > 1 && cv.length > 1)
            changed = changed || !lv[1].equals(cv[1]); // Minor
        if (!changed && lv.length > 2 && cv.length > 2)
            changed = changed || !lv[2].equals(cv[2]); // Patch

        return changed;
    }

    /**
     * Gets the associated plugin instance.
     *
     * @return The plugin instance.
     */
    public Plugin getPlugin() {
        return plugin;
    }

    /**
     * Loads AI AddOns from the "addons" folder.
     */
    private void loadAddOns() {
        loadExtensions("addons", "AddOn");
    }

    /**
     * Loads AI DLCs from the "dlcs" folder.
     */
    private void loadDLCs() {
        loadExtensions("dlcs", "DLC");
    }

    /**
     * Loads extensions (AddOns or DLCs) from the specified folder.
     * Scans JAR files for classes with an "onLoad(Plugin)" method and invokes them.
     *
     * @param folderName The folder name (relative to the plugin data folder).
     * @param type       The extension type label (e.g., "AddOn", "DLC").
     */
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
                            onLoadMethod = clazz.getMethod("onLoad", Plugin.class);
                        } catch (NoSuchMethodException e) {
                            continue;
                        }

                        Object extensionInstance = clazz.getDeclaredConstructor().newInstance();
                        onLoadMethod.invoke(extensionInstance, plugin);

                        logger.info("[" + type + "] Loaded: " + className);

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

    /**
     * Parses an expiration date from a supported input.
     *
     * @param input The date input (String or Date).
     * @return The parsed Date object.
     * @throws ParseException If the input string cannot be parsed.
     * @throws IllegalArgumentException If the input type is unsupported.
     */
    private static Date parseExpirationDate(Object input) throws ParseException {
        if (input instanceof Date) {
            return (Date) input;
        } else if (input instanceof String) {
            return DATE_FORMAT.parse((String) input);
        } else {
            throw new IllegalArgumentException("Unsupported expiration date input: " + input);
        }
    }

    /**
     * Validates a token against the plugin name, secret key, and current date.
     *
     * @param pluginName The plugin name.
     * @param secretKey The secret key used for validation.
     * @param token The token string to validate.
     * @param nowDateInput The current date or date string.
     * @return True if the token is valid and not expired; false otherwise.
     */
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

    /**
     * Extracts the expiration date from a token.
     *
     * @param pluginName The plugin name.
     * @param secretKey The secret key used for validation.
     * @param token The token string.
     * @return The extracted expiration date, or a date representing epoch time (0) if invalid.
     */
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
     * Registers an AI model instance if not already cached.
     * Called once per platform/model to initialize.
     *
     * @param platform AI platform
     * @param model    model name
     */
    public void registerModel(String platform, String model) {
        platform = platform.toLowerCase(); // normalize
        synchronized (modelCache) {
            modelCache.putIfAbsent(platform, new HashMap<>());
            Map<String, IMCEngineArtificialIntelligenceApiModel> platformMap = modelCache.get(platform);

            if (platformMap.containsKey(model)) {
                return; // Already registered
            }

            IMCEngineArtificialIntelligenceApiModel aiModel;
            switch (platform) {
                case "openai":
                    aiModel = new MCEngineArtificialIntelligenceApiModelOpenAi(plugin, model);
                    break;
                case "deepseek":
                    aiModel = new MCEngineArtificialIntelligenceApiModelDeepSeek(plugin, model);
                    break;
                case "openrouter":
                    aiModel = new MCEngineArtificialIntelligenceApiModelOpenRouter(plugin, model);
                    break;
                case "customurl":
                    aiModel = new MCEngineArtificialIntelligenceApiModelCustomUrl(plugin, model);
                    break;
                default:
                    throw new IllegalArgumentException("Unsupported AI platform: " + platform);
            }

            platformMap.put(model, aiModel);
            logger.info("Registered model → platform=" + platform + ", model=" + model);
        }
    }

    /**
     * Retrieves the registered AI model instance.
     *
     * @param platform AI platform
     * @param model    model name
     * @return AI model instance
     */
    public IMCEngineArtificialIntelligenceApiModel getAi(String platform, String model) {
        platform = platform.toLowerCase(); // normalize
        synchronized (modelCache) {
            Map<String, IMCEngineArtificialIntelligenceApiModel> platformMap = modelCache.get(platform);
            if (platformMap == null || !platformMap.containsKey(model)) {
                throw new IllegalStateException("AI model not registered → platform=" + platform + ", model=" + model);
            }
            return platformMap.get(model);
        }
    }

    /**
     * Shortcut to get response from AI.
     *
     * @param platform AI platform
     * @param model    model name
     * @param message  prompt message
     * @return response from AI
     */
    public String getResponse(String platform, String model, String message) {
        return getAi(platform, model).getResponse(message);
    }

    /**
     * Sends a message to the DeepSeek AI model and returns its response.
     *
     * @param message The message or prompt to send to DeepSeek.
     * @return The response from DeepSeek.
     */
    public String getResponseFromDeepSeek(String message) {
        return aiDeepSeek.getResponse(message);
    }

    /**
     * Sends a message to the OpenAI AI model and returns its response.
     *
     * @param message The message or prompt to send to OpenAI.
     * @return The response from OpenAI.
     */
    public String getResponseFromOpenAi(String message) {
        return aiOpenAi.getResponse(message);
    }

    /**
     * Sends a message to the OpenRouter AI model and returns its response.
     *
     * @param message The message or prompt to send to OpenRouter.
     * @return The response from OpenRouter.
     */
    public String getResponseFromOpenRouter(String message) {
        return aiOpenRouter.getResponse(message);
    }

    /**
     * Sends a message to the Custom URL AI model and returns its response.
     *
     * @param message The message or prompt to send to the custom URL AI.
     * @return The response from the custom URL AI.
     */
    public String getResponseFromCustomUrl(String message) {
        return aiCustomUrl.getResponse(message);
    }
}
