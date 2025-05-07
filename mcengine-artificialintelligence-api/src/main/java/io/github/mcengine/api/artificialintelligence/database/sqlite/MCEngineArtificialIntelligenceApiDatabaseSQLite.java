package io.github.mcengine.api.artificialintelligence.database.sqlite;

import io.github.mcengine.api.artificialintelligence.database.IMCEngineArtificialIntelligenceApiDatabase;
import io.github.mcengine.api.artificialintelligence.util.MCEngineArtificialIntelligenceApiUtilToken;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.sql.*;

/**
 * SQLite implementation for the AI API database.
 * Handles encrypted player token storage and retrieval.
 */
public class MCEngineArtificialIntelligenceApiDatabaseSQLite implements IMCEngineArtificialIntelligenceApiDatabase {

    private final String databaseUrl;

    /**
     * Constructs a new SQLite database handler from plugin config.
     * Path is retrieved from config key: database.sqlite.path
     *
     * @param plugin The Bukkit plugin instance.
     */
    public MCEngineArtificialIntelligenceApiDatabaseSQLite(Plugin plugin) {
        String fileName = plugin.getConfig().getString("database.sqlite.path", "artificialintelligence.db");
        File dbFile = new File(plugin.getDataFolder(), fileName);
        this.databaseUrl = "jdbc:sqlite:" + dbFile.getAbsolutePath();
        createTable();
    }

    /**
     * Creates the 'artificialintelligence' table if it does not exist.
     * Columns: id (auto-increment), player_uuid, platform, token.
     */
    public void createTable() {
        String sql = """
            CREATE TABLE IF NOT EXISTS artificialintelligence (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                player_uuid TEXT NOT NULL,
                platform TEXT NOT NULL,
                token TEXT NOT NULL,
                UNIQUE(player_uuid, platform)
            );
        """;

        try (Connection conn = DriverManager.getConnection(databaseUrl);
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Sets or updates the encrypted token for a given player UUID and platform.
     *
     * @param playerUuid The UUID of the player.
     * @param platform   The AI platform.
     * @param token      The raw (unencrypted) token to store.
     * @return Status message of the operation.
     */
    public String setPlayerToken(String playerUuid, String platform, String token) {
        String encryptedToken = MCEngineArtificialIntelligenceApiUtilToken.encryptToken(token);

        String sql = """
            INSERT INTO artificialintelligence (player_uuid, platform, token)
            VALUES (?, ?, ?)
            ON CONFLICT(player_uuid, platform) DO UPDATE SET token = excluded.token;
        """;

        try (Connection conn = DriverManager.getConnection(databaseUrl);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, playerUuid);
            stmt.setString(2, platform);
            stmt.setString(3, encryptedToken);
            stmt.executeUpdate();
            return "Token saved successfully.";
        } catch (SQLException e) {
            e.printStackTrace();
            return "Failed to save token.";
        }
    }

    /**
     * Retrieves the encrypted token for a player on a specific platform.
     *
     * @param playerUuid The UUID of the player.
     * @param platform   The AI platform.
     * @return The encrypted token or null if not found.
     */
    public String getPlayerToken(String playerUuid, String platform) {
        String sql = "SELECT token FROM artificialintelligence WHERE player_uuid = ? AND platform = ?";

        try (Connection conn = DriverManager.getConnection(databaseUrl);
            PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, playerUuid);
            stmt.setString(2, platform);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString("token"); // return ENCRYPTED token directly
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}
