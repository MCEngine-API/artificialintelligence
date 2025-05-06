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
 * DeepSeek implementation of {@link IMCEngineArtificialIntelligenceApiModel}.
 * This class communicates with the DeepSeek API to fetch AI-generated responses based on user prompts.
 */
public class MCEngineArtificialIntelligenceApiModelDeepSeek implements IMCEngineArtificialIntelligenceApiModel {

    private Plugin plugin;
    private String token;
    private String aiModel;

    public MCEngineArtificialIntelligenceApiModelDeepSeek(Plugin plugin) {
        initialize(plugin, plugin.getConfig().getString("ai.deepseek.model", "deepseek-chat"));
    }

    public MCEngineArtificialIntelligenceApiModelDeepSeek(Plugin plugin, String model) {
        initialize(plugin, model);
    }

    private void initialize(Plugin plugin, String model) {
        this.plugin = plugin;
        this.token = plugin.getConfig().getString("ai.deepseek.token", null);
        this.aiModel = model;
        plugin.getLogger().info("Platform: DeepSeek");
        plugin.getLogger().info("Model: " + aiModel);
    }

    /**
     * Sends a user message to the DeepSeek API and retrieves the AI's response.
     *
     * @param message The user message or prompt to send.
     * @return The AI's response as a string, or an error message if the request fails.
     */
    @Override
    public String getResponse(String message) {
        try {
            URI uri = URI.create("https://api.deepseek.com/v1/chat/completions");
            URL url = uri.toURL();
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            conn.setRequestMethod("POST");
            conn.setRequestProperty("Authorization", "Bearer " + token);
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
