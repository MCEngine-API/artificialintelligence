package io.github.mcengine.api.artificialintelligence.database;

/**
 * Interface for AI API database operations related to storing and retrieving user tokens.
 */
public interface IMCEngineArtificialIntelligenceApiDatabase {

    /**
     * Sets or updates the token for a given player UUID.
     *
     * @param playerUuid The UUID of the player.
     * @param token      The token to associate with the player.
     * @return A message or status result of the operation.
     */
    void setPlayerToken(String playerUuid, String platform, String token);

    /**
     * Retrieves the token associated with a specific player UUID.
     *
     * @param playerUuid The UUID of the player.
     * @return The token for the player, or null if not found.
     */
    String getPlayerToken(String playerUuid, String platform);
}
