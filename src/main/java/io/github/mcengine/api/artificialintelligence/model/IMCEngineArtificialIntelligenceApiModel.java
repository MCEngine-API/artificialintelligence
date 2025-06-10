package io.github.mcengine.api.artificialintelligence.model;

/**
 * Interface for AI response providers.
 * Implementing classes should provide logic to generate a response based on the input message.
 */
public interface IMCEngineArtificialIntelligenceApiModel {

    /**
     * Generates a response from the AI based on the given message.
     *
     * @param message The input message or prompt to the AI.
     * @return The AI-generated response.
     */
    String getResponse(String message);

    /**
     * Generates a response from the AI using a user-specific token.
     *
     * @param token   The user-specific token for authenticating or identifying the request.
     * @param message The input message or prompt to the AI.
     * @return The AI-generated response.
     */
    String getResponse(String token, String message);
}