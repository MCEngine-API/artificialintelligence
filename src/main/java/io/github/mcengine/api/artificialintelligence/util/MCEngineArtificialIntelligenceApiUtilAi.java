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
 * Utility class for AI model registration and API interaction.
 */
public class MCEngineArtificialIntelligenceApiUtilAi {

    /**
     * Global cache shared across all plugins.
     * Maps platform name to its models and instances.
     */
    private static final Map<String, Map<String, IMCEngineArtificialIntelligenceApiModel>> modelCache = new HashMap<>();

    /**
     * Sends a prompt to an AI API and returns the full JSON response.
     *
     * @param plugin       The Bukkit plugin instance.
     * @param endpoint     API endpoint URL.
     * @param aiModel      Model name (e.g., "gpt-4").
     * @param defaultToken Server default token.
     * @param token        User or provided token.
     * @param systemPrompt The system prompt to guide AI behavior.
     * @param message      User prompt content.
     * @param isOpenRouter Whether to include OpenRouter headers.
     * @return The raw JSON response from the AI API, or error message in JSON format.
     */
    public static JsonObject getResponse(
            Plugin plugin,
            String endpoint,
            String aiModel,
            String defaultToken,
            String token,
            String systemPrompt,
            String message,
            boolean isOpenRouter
    ) {
        if (token == null || token.isEmpty()) {
            plugin.getLogger().severe("Token is missing or invalid.");
            JsonObject error = new JsonObject();
            error.addProperty("error", "Missing or invalid token.");
            return error;
        }

        String actualToken = token;

        if (!token.equals(defaultToken)) {
            actualToken = MCEngineArtificialIntelligenceApiUtilToken.decryptToken(token);
            if (actualToken == null || actualToken.isEmpty()) {
                plugin.getLogger().warning("Failed to decrypt user token.");
                JsonObject error = new JsonObject();
                error.addProperty("error", "Invalid or corrupt user token.");
                return error;
            }
        }

        try {
            URI uri = URI.create(endpoint);
            URL url = uri.toURL();
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            conn.setRequestMethod("POST");
            conn.setRequestProperty("Authorization", "Bearer " + actualToken);
            conn.setRequestProperty("Content-Type", "application/json");

            if (isOpenRouter) {
                conn.setRequestProperty("HTTP-Referer", "https://github.com/mcengine");
                conn.setRequestProperty("X-Title", "MCEngine AI");
            }

            conn.setDoOutput(true);

            JsonObject payload = new JsonObject();
            payload.addProperty("model", aiModel);
            payload.addProperty("temperature", 0.7);

            JsonArray messages = new JsonArray();

            if (systemPrompt != null && !systemPrompt.isEmpty()) {
                JsonObject systemMessage = new JsonObject();
                systemMessage.addProperty("role", "system");
                systemMessage.addProperty("content", systemPrompt);
                messages.add(systemMessage);
            }

            JsonObject userMessage = new JsonObject();
            userMessage.addProperty("role", "user");
            userMessage.addProperty("content", message);
            messages.add(userMessage);

            payload.add("messages", messages);

            try (OutputStream os = conn.getOutputStream()) {
                os.write(payload.toString().getBytes(StandardCharsets.UTF_8));
            }

            int statusCode = conn.getResponseCode();
            if (statusCode != 200) {
                plugin.getLogger().warning("AI API returned status: " + statusCode);
                JsonObject error = new JsonObject();
                error.addProperty("error", "API request failed with status code: " + statusCode);
                return error;
            }

            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
            StringBuilder responseBuilder = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null) {
                responseBuilder.append(line);
            }
            in.close();

            return JsonParser.parseString(responseBuilder.toString()).getAsJsonObject();
        } catch (Exception e) {
            plugin.getLogger().severe("AI API error: " + e.getMessage());
            JsonObject error = new JsonObject();
            error.addProperty("error", "Exception: " + e.getMessage());
            return error;
        }
    }

    /**
     * Extracts the completion content from a full response JSON.
     *
     * @param responseJson The full JSON response from the API.
     * @return The generated message content or fallback string.
     */
    public static String getCompletionContent(JsonObject responseJson) {
        try {
            JsonArray choices = responseJson.getAsJsonArray("choices");
            if (choices != null && choices.size() > 0) {
                JsonObject messageObj = choices.get(0).getAsJsonObject().getAsJsonObject("message");
                return messageObj.get("content").getAsString().trim();
            }
        } catch (Exception e) {
            return "Error parsing content: " + e.getMessage();
        }
        return "No response from AI.";
    }

    /**
     * Extracts total token usage from the response JSON.
     *
     * @param responseJson The full JSON response from the API.
     * @return Total token usage as integer, or -1 if not available.
     */
    public static int getTotalTokenUsage(JsonObject responseJson) {
        try {
            JsonObject usage = responseJson.getAsJsonObject("usage");
            if (usage != null && usage.has("total_tokens")) {
                return usage.get("total_tokens").getAsInt();
            }
        } catch (Exception ignored) {
        }
        return -1;
    }

    /**
     * Registers an AI model into the cache for given platform and model name.
     *
     * @param plugin   The Bukkit plugin instance.
     * @param platform AI platform name.
     * @param model    Model name.
     */
    public static void registerModel(Plugin plugin, String platform, String model) {
        Logger logger = plugin.getLogger();
        platform = platform.toLowerCase();

        synchronized (modelCache) {
            modelCache.putIfAbsent(platform, new HashMap<>());
            Map<String, IMCEngineArtificialIntelligenceApiModel> platformMap = modelCache.get(platform);

            if (platformMap.containsKey(model)) {
                return;
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
     * Retrieves the AI model instance from cache.
     *
     * @param platform AI platform name.
     * @param model    Model name.
     * @return AI model instance.
     */
    public static IMCEngineArtificialIntelligenceApiModel getAi(String platform, String model) {
        platform = platform.toLowerCase();
        synchronized (modelCache) {
            Map<String, IMCEngineArtificialIntelligenceApiModel> platformMap = modelCache.get(platform);
            if (platformMap == null || !platformMap.containsKey(model)) {
                throw new IllegalStateException("AI model not registered → platform=" + platform + ", model=" + model);
            }
            return platformMap.get(model);
        }
    }

    /**
     * Returns a shallow copy of all currently registered AI models.
     *
     * @return Map of platform names to their model instances.
     */
    public static Map<String, Map<String, ?>> getAllModels() {
        synchronized (modelCache) {
            return new HashMap<>(modelCache);
        }
    }
}
