package io.github.mcengine.api.artificialintelligence.util;

import io.github.mcengine.api.artificialintelligence.model.*;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Utility class for AI model registration and caching.
 */
public class MCEngineArtificialIntelligenceApiUtilAi {

    // 🔥 GLOBAL cache shared across ALL plugins (platform → (model → instance))
    private static final Map<String, Map<String, IMCEngineArtificialIntelligenceApiModel>> modelCache = new HashMap<>();

    /**
     * Registers an AI model instance if not already cached.
     *
     * @param plugin   The Bukkit plugin instance.
     * @param platform AI platform name.
     * @param model    Model name.
     */
    public static void registerModel(Plugin plugin, String platform, String model) {
        Logger logger = plugin.getLogger();
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
                    String[] parts = model.split(":", 2);
                    String server = parts[0];
                    String actualModel = (parts.length > 1) ? parts[1] : plugin.getConfig().getString("ai.custom." + server + ".model", null);
                    aiModel = new MCEngineArtificialIntelligenceApiModelCustomUrl(plugin, server, actualModel);
                    break;
                default:
                    throw new IllegalArgumentException("Unsupported AI platform: " + platform);
            }

            platformMap.put(model, aiModel);
            logger.info("*".repeat(15));
            logger.info("Registered AI");
            logger.info("Platform: " + platform);
            logger.info("Model: " + model);
            logger.info("*".repeat(15));
        }
    }

    /**
     * Retrieves a registered AI model.
     *
     * @param platform AI platform.
     * @param model    Model name.
     * @return AI model instance.
     */
    public static IMCEngineArtificialIntelligenceApiModel getAi(String platform, String model) {
        platform = platform.toLowerCase(); // normalize
        synchronized (modelCache) {
            Map<String, IMCEngineArtificialIntelligenceApiModel> platformMap = modelCache.get(platform);
            if (platformMap == null || !platformMap.containsKey(model)) {
                throw new IllegalStateException("AI model not registered → platform=" + platform + ", model=" + model);
            }
            return platformMap.get(model);
        }
    }
}
