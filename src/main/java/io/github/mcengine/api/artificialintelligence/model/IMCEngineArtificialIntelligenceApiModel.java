package io.github.mcengine.api.artificialintelligence.model;

import com.google.gson.JsonObject;

/**
 * Interface for AI response providers.
 * Implementing classes must provide logic to generate a full JSON response based on input.
 */
public interface IMCEngineArtificialIntelligenceApiModel {

    /**
     * Generates a full JSON response from the AI using the default token.
     *
     * @param systemPrompt The system prompt providing instructions or behavior guidance.
     * @param message      The input message or prompt to the AI.
     * @return A {@link JsonObject} containing the full AI response.
     */
    JsonObject getResponse(String systemPrompt, String message);

    /**
     * Generates a full JSON response from the AI using a user-specific token.
     *
     * @param token        The user-specific token for authenticating or identifying the request.
     * @param systemPrompt The system prompt providing instructions or behavior guidance.
     * @param message      The input message or prompt to the AI.
     * @return A {@link JsonObject} containing the full AI response.
     */
    JsonObject getResponse(String token, String systemPrompt, String message);
}
