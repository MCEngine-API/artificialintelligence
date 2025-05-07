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

import io.github.mcengine.api.artificialintelligence.util.MCEngineArtificialIntelligenceApiUtilToken;

/**
 * DeepSeek implementation of {@link IMCEngineArtificialIntelligenceApiModel}.
 * This class communicates with the DeepSeek API to fetch AI-generated responses based on user prompts.
 */
public class MCEngineArtificialIntelligenceApiModelDeepSeek implements IMCEngineArtificialIntelligenceApiModel {

    /**
     * The Bukkit plugin instance used for accessing configuration and logging.
     */
    private final Plugin plugin;

    /**
     * The default token configured for accessing the DeepSeek API.
     * Used when a user-specific token is not provided.
     */
    private final String defaultToken;

    /**
     * The name of the AI model to be used when sending requests to DeepSeek.
     */
    private final String aiModel;

    /**
     * Constructs a DeepSeek model handler instance using configuration and model name.
     *
     * @param plugin The Bukkit plugin instance for accessing configuration and logging.
     * @param model  The name of the AI model to use (e.g., "deepseek-chat").
     */
    public MCEngineArtificialIntelligenceApiModelDeepSeek(Plugin plugin, String model) {
        this.plugin = plugin;
        this.defaultToken = plugin.getConfig().getString("ai.deepseek.token", null);
        this.aiModel = model;
    }

    /**
     * Sends a message to the DeepSeek API using the default token from the configuration.
     *
     * @param message The user input message or prompt to send.
     * @return The AI-generated response as a string.
     */
    @Override
    public String getResponse(String message) {
        return getResponse(defaultToken, message);
    }

    /**
     * Sends a message to the DeepSeek API using the provided user-specific token.
     *
     * @param token   The user-specific authentication token for DeepSeek.
     * @param message The user input message or prompt to send.
     * @return The AI-generated response as a string, or an error message if the request fails.
     */
    @Override
    public String getResponse(String token, String message) {
        if (token == null || token.isEmpty()) {
            plugin.getLogger().severe("DeepSeek token is missing or invalid.");
            return "Error: Missing or invalid DeepSeek token.";
        }

        String actualToken = token;

        // 🔐 Decrypt token only if it's different from the server token
        if (!token.equals(defaultToken)) {
            actualToken = MCEngineArtificialIntelligenceApiUtilToken.decryptToken(token);
            if (actualToken == null || actualToken.isEmpty()) {
                plugin.getLogger().warning("Unable to decrypt the user token for DeepSeek");
                return "Error: Invalid or corrupt user token.";
            }
        }

        try {
            URI uri = URI.create("https://api.deepseek.com/v1/chat/completions");
            URL url = uri.toURL();
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            conn.setRequestMethod("POST");
            conn.setRequestProperty("Authorization", "Bearer " + actualToken);
            conn.setRequestProperty("Content-Type", "application/json");
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
                os.write(payload.toString().getBytes(StandardCharsets.UTF_8));
            }

            int statusCode = conn.getResponseCode();
            if (statusCode != 200) {
                plugin.getLogger().warning("DeepSeek API returned status: " + statusCode);
                return "Error: Unable to get response from DeepSeek API";
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

            return "No response from DeepSeek AI.";
        } catch (Exception e) {
            plugin.getLogger().severe("DeepSeek API error: " + e.getMessage());
            return "Exception: " + e.getMessage();
        }
    }
}
