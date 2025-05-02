package io.github.mcengine.api.artificialintelligence.model;

import io.github.mcengine.api.artificialintelligence.model.IMCEngineArtificialIntelligenceApiModel;
import org.bukkit.plugin.Plugin;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;

/**
 * Custom URL AI implementation of {@link IMCEngineArtificialIntelligenceApiModel}.
 * Communicates with a user-defined API endpoint specified in the plugin configuration.
 */
public class MCEngineArtificialIntelligenceApiModelCustomUrl implements IMCEngineArtificialIntelligenceApiModel {

    private final Plugin plugin;
    private final String token;
    private final String aiModel;
    private final String endpoint;

    /**
     * Constructs a new Custom URL AI model handler.
     *
     * @param plugin The Bukkit plugin instance to retrieve configuration and logger.
     */
    public MCEngineArtificialIntelligenceApiModelCustomUrl(Plugin plugin) {
        this.plugin = plugin;
        this.token = plugin.getConfig().getString("ai.custom.token", null);
        this.aiModel = plugin.getConfig().getString("ai.custom.model", "gpt-3.5-turbo");
        this.endpoint = plugin.getConfig().getString("ai.custom.url", "http://localhost:11434/v1/chat/completions");
        plugin.getLogger().info("Using Custom URL at " + endpoint);
        plugin.getLogger().info("Model: " + aiModel);
    }

    /**
     * Sends a user message to the custom API endpoint and returns the AI's response.
     *
     * @param message The user input message to send.
     * @return The AI-generated response string.
     */
    @Override
    public String getResponse(String message) {
        try {
            URI uri = URI.create(endpoint);
            URL url = uri.toURL();
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            conn.setRequestMethod("POST");
            if (token != null && !token.isEmpty()) {
                conn.setRequestProperty("Authorization", "Bearer " + token);
            }
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            JSONObject payload = new JSONObject();
            payload.put("model", aiModel);
            payload.put("temperature", 0.7);

            JSONArray messages = new JSONArray();
            JSONObject userMessage = new JSONObject();
            userMessage.put("role", "user");
            userMessage.put("content", message);
            messages.put(userMessage);
            payload.put("messages", messages);

            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = payload.toString().getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            int statusCode = conn.getResponseCode();
            if (statusCode != 200) {
                plugin.getLogger().warning("Custom URL API returned status: " + statusCode);
                return "Error: Unable to get response from Custom URL API";
            }

            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder responseBuilder = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null) {
                responseBuilder.append(line);
            }
            in.close();

            JSONObject responseJson = new JSONObject(responseBuilder.toString());
            JSONArray choices = responseJson.getJSONArray("choices");

            if (choices.length() > 0) {
                JSONObject messageObj = choices.getJSONObject(0).getJSONObject("message");
                return messageObj.getString("content").trim();
            }

            return "No response from Custom URL AI.";
        } catch (Exception e) {
            plugin.getLogger().severe("Custom URL API error: " + e.getMessage());
            return "Exception: " + e.getMessage();
        }
    }
}
