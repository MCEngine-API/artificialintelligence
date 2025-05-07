package io.github.mcengine.api.artificialintelligence.model;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.bukkit.plugin.Plugin;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * OpenRouter implementation of {@link IMCEngineArtificialIntelligenceApiModel}.
 * This class communicates with the OpenRouter API to fetch AI-generated responses.
 */
public class MCEngineArtificialIntelligenceApiModelOpenRouter implements IMCEngineArtificialIntelligenceApiModel {

    /**
     * The Bukkit plugin instance used to access configuration and log errors/warnings.
     */
    private final Plugin plugin;

    /**
     * The default authentication token used when a user-specific token is not supplied.
     * Retrieved from the config path {@code ai.openrouter.token}.
     */
    private final String defaultToken;

    /**
     * The AI model name (e.g., "openrouter/gpt-4") to use for API calls.
     */
    private final String aiModel;

    /**
     * Constructs a new OpenRouter AI model handler using a specified model.
     *
     * @param plugin The Bukkit plugin instance to retrieve configuration and logger.
     * @param model  The AI model name to use.
     */
    public MCEngineArtificialIntelligenceApiModelOpenRouter(Plugin plugin, String model) {
        this.plugin = plugin;
        this.defaultToken = plugin.getConfig().getString("ai.openrouter.token", null);
        this.aiModel = model;
    }

    /**
     * Sends a user message to the OpenRouter API using the default token from configuration.
     *
     * @param message The user input message to send.
     * @return The AI-generated response string.
     */
    @Override
    public String getResponse(String message) {
        return getResponse(defaultToken, message);
    }

    /**
     * Sends a user message to the OpenRouter API using a provided user-specific token.
     *
     * @param token   A user-specific authentication token to use for the API call.
     * @param message The user input message to send to the AI.
     * @return The AI-generated response string, or an error message if the request fails.
     */
    @Override
    public String getResponse(String token, String message) {
        if (token == null || token.isEmpty()) {
            plugin.getLogger().severe("OpenRouter token is missing or invalid.");
            return "Error: Missing or invalid OpenRouter token.";
        }

        String actualToken = token;

        // 🔐 Decrypt token only if it's different from the server token
        if (!token.equals(defaultToken)) {
            actualToken = MCEngineArtificialIntelligenceApiUtilToken.decryptToken(token);
            if (actualToken == null || actualToken.isEmpty()) {
                plugin.getLogger().warning("Unable to decrypt the user token for OpenRouter");
                return "Error: Invalid or corrupt user token.";
            }
        }

        try {
            URI uri = URI.create("https://openrouter.ai/api/v1/chat/completions");
            URL url = uri.toURL();
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            conn.setRequestMethod("POST");
            conn.setRequestProperty("Authorization", "Bearer " + actualToken);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("HTTP-Referer", "https://github.com/mcengine");
            conn.setRequestProperty("X-Title", "MCEngine AI");
            conn.setDoOutput(true);

            JsonObject payload = new JsonObject();
            payload.addProperty("model", aiModel);
            payload.addProperty("temperature", 0.7);

            JsonArray messages = new JsonArray();
            JsonObject userMessage = new JsonObject();
            userMessage.addProperty("role", "user");
            userMessage.addProperty("content", message);
            messages.add(userMessage);

            payload.add("messages", messages);

            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = payload.toString().getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            int statusCode = conn.getResponseCode();
            if (statusCode != 200) {
                plugin.getLogger().warning("OpenRouter API returned status: " + statusCode);
                return "Error: Unable to get response from OpenRouter API";
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

            return "No response from OpenRouter AI.";
        } catch (Exception e) {
            plugin.getLogger().severe("OpenRouter API error: " + e.getMessage());
            return "Exception: " + e.getMessage();
        }
    }
}
