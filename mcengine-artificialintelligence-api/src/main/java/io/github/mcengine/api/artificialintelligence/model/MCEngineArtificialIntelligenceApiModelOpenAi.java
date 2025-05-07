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
import java.nio.charset.StandardCharsets;

import io.github.mcengine.api.artificialintelligence.util.MCEngineArtificialIntelligenceApiUtilToken;

/**
 * OpenAI API implementation of {@link IMCEngineArtificialIntelligenceApiModel}.
 * Communicates with the OpenAI Chat API using the configured model and token.
 */
public class MCEngineArtificialIntelligenceApiModelOpenAi implements IMCEngineArtificialIntelligenceApiModel {

    /**
     * The Bukkit plugin instance used for accessing configuration and logging.
     */
    private final Plugin plugin;

    /**
     * The default token used to authenticate with the OpenAI API.
     * Pulled from config at {@code ai.openai.token}.
     */
    private final String defaultToken;

    /**
     * The model name (e.g., gpt-4, gpt-3.5-turbo) used in requests to the OpenAI API.
     */
    private final String aiModel;

    /**
     * Constructs an OpenAI API model integration using plugin config for credentials and endpoint setup.
     *
     * @param plugin The Bukkit plugin instance.
     * @param model  The name of the OpenAI model to use for responses.
     */
    public MCEngineArtificialIntelligenceApiModelOpenAi(Plugin plugin, String model) {
        this.plugin = plugin;
        this.defaultToken = plugin.getConfig().getString("ai.openai.token", null);
        this.aiModel = model;
    }

    /**
     * Sends a user message to the OpenAI API using the default token from configuration.
     *
     * @param message The user input message or prompt to send.
     * @return The AI-generated response from OpenAI.
     */
    @Override
    public String getResponse(String message) {
        return getResponse(defaultToken, message);
    }

    /**
     * Sends a user message to the OpenAI API using a provided user-specific token and returns the response.
     *
     * @param token   A user-specific token used for authenticating the request.
     * @param message The input message or prompt to send to the AI.
     * @return The AI-generated response string, or an error message if the request fails.
     */
    @Override
    public String getResponse(String token, String message) {
        if (token == null || token.isEmpty()) {
            plugin.getLogger().severe("OpenAi token is missing or invalid.");
            return "Error: Missing or invalid OpenAi token.";
        }

        String actualToken = token;

        // 🔐 Decrypt token only if it's different from the server token
        if (!token.equals(defaultToken)) {
            actualToken = MCEngineArtificialIntelligenceApiUtilToken.decryptToken(token);
            if (actualToken == null || actualToken.isEmpty()) {
                plugin.getLogger().warning("Unable to decrypt the user token for OpenAi");
                return "Error: Invalid or corrupt user token.";
            }
        }

        try {
            URI uri = URI.create("https://api.openai.com/v1/chat/completions");
            HttpURLConnection conn = (HttpURLConnection) uri.toURL().openConnection();

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
