package io.github.mcengine.api.artificialintelligence.database;

/**
 * Interface for AI API database operations related to storing and retrieving user tokens.
 * <p>
 * Implementations may be SQL (e.g., MySQL, PostgreSQL, SQLite) or NoSQL; the contract
 * therefore includes generic query helpers in addition to typed token operations.
 */
public interface IMCEngineArtificialIntelligenceDB {

    /**
     * Executes a backend-specific non-returning command (DDL/DML).
     * <p>For SQL backends, this will typically be raw SQL; for NoSQL backends, a DSL/JSON string.</p>
     *
     * @param query command to execute
     */
    void executeQuery(String query);

    /**
     * Executes a backend-specific query that returns a single scalar value
     * (one row, one column).
     * <p>Supported {@code type} values commonly include {@code String}, {@code Integer},
     * {@code Long}, {@code Double}, and {@code Boolean}.</p>
     *
     * @param query command/query to execute
     * @param type  expected Java type of the single result
     * @param <T>   generic result type
     * @return value if present; otherwise {@code null}
     * @throws IllegalArgumentException if {@code type} is unsupported by the implementation
     */
    <T> T getValue(String query, Class<T> type);

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
     * @return The token for the player, or {@code null} if not found.
     */
    String getPlayerToken(String playerUuid, String platform);
}
