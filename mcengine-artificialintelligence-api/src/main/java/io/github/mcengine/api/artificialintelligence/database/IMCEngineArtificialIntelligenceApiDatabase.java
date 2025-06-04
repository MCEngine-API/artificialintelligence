package io.github.mcengine.api.artificialintelligence.database;

import java.sql.Connection;

/**
 * Interface for AI API database operations related to storing and retrieving user tokens.
 */
public interface IMCEngineArtificialIntelligenceApiDatabase {

    /**
     * Gets the database connection.
     *
     * @return A {@link Connection} to the database.
     */
    Connection getDBConnection();

    /**
     * Sets or updates the token for a given player UUID.
     *
     * @param playerUuid The UUID of the player.
     * @param platform   The platform name.
     * @param token      The token to associate with the player.
     */
    void setPlayerToken(String playerUuid, String platform, String token);

    /**
     * Retrieves the token associated with a specific player UUID.
     *
     * @param playerUuid The UUID of the player.
     * @param platform   The platform name.
     * @return The token for the player, or null if not found.
     */
    String getPlayerToken(String playerUuid, String platform);
}
