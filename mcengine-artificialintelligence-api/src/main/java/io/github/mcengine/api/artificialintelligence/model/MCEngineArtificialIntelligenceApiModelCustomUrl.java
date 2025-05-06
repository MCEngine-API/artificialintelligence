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
 * Custom URL AI implementation of {@link IMCEngineArtificialIntelligenceApiModel}.
 * Communicates with a user-defined API endpoint specified in the plugin configuration.
 */
public class MCEngineArtificialIntelligenceApiModelCustomUrl implements IMCEngineArtificialIntelligenceApiModel {

    private Plugin plugin;
    private String token;
    private String endpoint;
    private String aiModel;

    /**
     * Constructs a new Custom URL AI model handler for a specific server and model.
     * <p>
     * This constructor retrieves the API endpoint URL, token, and default model
     * from the plugin configuration path {@code ai.custom.{server}}.
     * It allows multiple servers, each with their own endpoint and token, to be used concurrently.
     *
     * @param plugin The Bukkit plugin instance for configuration and logging.
     * @param server The server identifier (corresponding to the config section {@code ai.custom.{server}}).
     * @param model  The AI model name; if null, uses the model from config {@code ai.custom.{server}.model}.
     */
    public MCEngineArtificialIntelligenceApiModelCustomUrl(Plugin plugin, String server, String model) {
        this.plugin = plugin;
        String configBase = "ai.custom." + server + ".";
        this.token = plugin.getConfig().getString(configBase + "token", null);
        this.endpoint = plugin.getConfig().getString(configBase + "url", "http://localhost:11434/v1/chat/completions");
        this.aiModel = plugin.getConfig().getString(configBase + "model", model);
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
                plugin.getLogger().warning("Custom URL API returned status: " + statusCode);
                return "Error: Unable to get response from Custom URL API";
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

            return "No response from Custom URL AI.";
        } catch (Exception e) {
            plugin.getLogger().severe("Custom URL API error: " + e.getMessage());
            return "Exception: " + e.getMessage();
        }
    }
}
