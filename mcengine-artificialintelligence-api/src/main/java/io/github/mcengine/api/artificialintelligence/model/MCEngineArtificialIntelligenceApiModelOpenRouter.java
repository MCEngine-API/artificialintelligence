package io.github.mcengine.api.artificialintelligence.model;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.github.mcengine.api.artificialintelligence.model.IMCEngineArtificialIntelligenceApiModel;
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

    private final Plugin plugin;
    private final String token;
    private final String aiModel;

    /**
     * Constructs a new OpenRouter AI model handler.
     *
     * @param plugin The Bukkit plugin instance to retrieve configuration and logger.
     */
    public MCEngineArtificialIntelligenceApiModelOpenRouter(Plugin plugin) {
        this.plugin = plugin;
        this.token = plugin.getConfig().getString("ai.openrouter.token", null);
        this.aiModel = plugin.getConfig().getString("ai.openrouter.model", "mistralai/mistral-7b-instruct");
        plugin.getLogger().info("Platform: OpenRouter");
        plugin.getLogger().info("Model: " + aiModel);
    }

    /**
     * Sends a user message to the OpenRouter API and returns the AI's response.
     *
     * @param message The user input message to send.
     * @return The AI-generated response string.
     */
    @Override
    public String getResponse(String message) {
        try {
            URI uri = URI.create("https://openrouter.ai/api/v1/chat/completions");
            URL url = uri.toURL();
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            conn.setRequestMethod("POST");
            conn.setRequestProperty("Authorization", "Bearer " + token);
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
