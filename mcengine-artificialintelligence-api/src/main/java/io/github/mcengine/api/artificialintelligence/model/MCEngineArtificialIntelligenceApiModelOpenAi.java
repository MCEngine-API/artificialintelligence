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
import java.nio.charset.StandardCharsets;

/**
 * OpenAI API implementation of {@link IMCEngineArtificialIntelligenceApiModel}.
 * Communicates with the OpenAI Chat API using the configured model and token.
 */
public class MCEngineArtificialIntelligenceApiModelOpenAi implements IMCEngineArtificialIntelligenceApiModel {

    private Plugin plugin;
    private String token;
    private String aiModel;

    public MCEngineArtificialIntelligenceApiModelOpenAi(Plugin plugin, String model) {
        this.plugin = plugin;
        this.token = plugin.getConfig().getString("ai.openai.token", null);
        this.aiModel = model;
    }

    /**
     * Sends a user message to the OpenAI API and retrieves the AI's response.
     *
     * @param message The user message or prompt to send.
     * @return The AI's response as a string, or an error message if the request fails.
     */
    @Override
    public String getResponse(String message) {
        try {
            URI uri = URI.create("https://api.openai.com/v1/chat/completions");
            HttpURLConnection conn = (HttpURLConnection) uri.toURL().openConnection();

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

            int responseCode = conn.getResponseCode();
            if (responseCode != 200) {
                plugin.getLogger().warning("OpenAI API returned status: " + responseCode);
                return "Error: OpenAI API request failed.";
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

            return "No response from OpenAI.";
        } catch (Exception e) {
            plugin.getLogger().severe("OpenAI API error: " + e.getMessage());
            return "Exception: " + e.getMessage();
        }
    }
}
