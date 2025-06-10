package io.github.mcengine.api.artificialintelligence.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.github.mcengine.api.artificialintelligence.model.*;
import org.bukkit.plugin.Plugin;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
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
     * Sends a prompt message to an AI model endpoint and retrieves the response.
     * This method supports token decryption (when user-specific tokens are used) and shared logic
     * for different APIs like OpenAI, OpenRouter, DeepSeek, and custom URLs.
     *
     * @param plugin       The Bukkit plugin instance used for logging.
     * @param endpoint     The API endpoint URL (e.g., https://api.openai.com/v1/chat/completions).
     * @param aiModel      The AI model identifier (e.g., "gpt-4", "deepseek-chat").
     * @param defaultToken The default API token configured in the plugin.
     * @param token        The user-specific token; if different from default, it will be decrypted.
     * @param message      The user message or prompt to send to the AI model.
     * @param isOpenRouter Whether to include OpenRouter-specific headers (Referer, X-Title).
     * @return The AI-generated response as a string, or an error message if something fails.
     */
    public static String getResponse(
            Plugin plugin,
            String endpoint,
            String aiModel,
            String defaultToken,
            String token,
            String message,
            boolean isOpenRouter
    ) {
        if (token == null || token.isEmpty()) {
            plugin.getLogger().severe("Token is missing or invalid.");
            return "Error: Missing or invalid token.";
        }

        String actualToken = token;

        // Decrypt the user token only if it differs from the default (server) token
        if (!token.equals(defaultToken)) {
            actualToken = MCEngineArtificialIntelligenceApiUtilToken.decryptToken(token);
            if (actualToken == null || actualToken.isEmpty()) {
                plugin.getLogger().warning("Failed to decrypt user token.");
                return "Error: Invalid or corrupt user token.";
            }
        }

        try {
            URI uri = URI.create(endpoint);
            URL url = uri.toURL();
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            conn.setRequestMethod("POST");
            conn.setRequestProperty("Authorization", "Bearer " + actualToken);
            conn.setRequestProperty("Content-Type", "application/json");

            // Optional headers for OpenRouter
            if (isOpenRouter) {
                conn.setRequestProperty("HTTP-Referer", "https://github.com/mcengine");
                conn.setRequestProperty("X-Title", "MCEngine AI");
            }

            conn.setDoOutput(true);

            // Build JSON payload
            JsonObject payload = new JsonObject();
            payload.addProperty("model", aiModel);
            payload.addProperty("temperature", 0.7);

            JsonArray messages = new JsonArray();
            JsonObject userMessage = new JsonObject();
            userMessage.addProperty("role", "user");
            userMessage.addProperty("content", message);
            messages.add(userMessage);
            payload.add("messages", messages);

            // Send request
            try (OutputStream os = conn.getOutputStream()) {
                os.write(payload.toString().getBytes(StandardCharsets.UTF_8));
            }

            // Handle response
            int statusCode = conn.getResponseCode();
            if (statusCode != 200) {
                plugin.getLogger().warning("AI API returned status: " + statusCode);
                return "Error: API request failed.";
            }

            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
            StringBuilder responseBuilder = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null) {
                responseBuilder.append(line);
            }
            in.close();

            JsonObject responseJson = JsonParser.parseString(responseBuilder.toString()).getAsJsonObject();
            JsonArray choices = responseJson.getAsJsonArray("choices");

            if (choices.size() > 0) {
                JsonObject messageObj = choices.get(0).getAsJsonObject().getAsJsonObject("message");
                return messageObj.get("content").getAsString().trim();
            }

            return "No response from AI.";
        } catch (Exception e) {
            plugin.getLogger().severe("AI API error: " + e.getMessage());
            return "Exception: " + e.getMessage();
        }
    }

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
                    if (parts.length != 2) {
                        throw new IllegalArgumentException("CustomURL model must be in format 'server:modelName'. Got: " + model);
                    }
                    String server = parts[0];
                    String actualModel = parts[1];
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

    /**
     * Returns a shallow copy of the current model cache containing all registered models.
     *
     * @return A map of platform names to their associated model instances.
     */
    public static Map<String, Map<String, ?>> getAllModels() {
        synchronized (modelCache) {
            return new HashMap<>(modelCache);
        }
    }
}
