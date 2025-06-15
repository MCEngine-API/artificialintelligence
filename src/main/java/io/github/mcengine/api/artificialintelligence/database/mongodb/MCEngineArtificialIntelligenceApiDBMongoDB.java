package io.github.mcengine.api.artificialintelligence.database.mongodb;

import com.mongodb.client.*;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ReplaceOptions;
import io.github.mcengine.api.artificialintelligence.database.IMCEngineArtificialIntelligenceApiDatabase;
import io.github.mcengine.api.artificialintelligence.util.MCEngineArtificialIntelligenceApiUtilToken;
import org.bson.Document;
import org.bukkit.plugin.Plugin;

import java.sql.Connection;

/**
 * MongoDB implementation of the AI API database.
 * Stores and retrieves encrypted player tokens in a document collection.
 */
public class MCEngineArtificialIntelligenceApiDBMongoDB implements IMCEngineArtificialIntelligenceApiDatabase {

    /** The Bukkit plugin instance. */
    private final Plugin plugin;

    /** MongoDB client instance. */
    private final MongoClient mongoClient;

    /** MongoDB database reference. */
    private final MongoDatabase database;

    /** MongoDB collection used for token storage. */
    private final MongoCollection<Document> collection;

    /**
     * Constructs a new MongoDB database handler using plugin config.
     * Required keys: database.mongodb.uri, database.mongodb.name, database.mongodb.collection.
     *
     * @param plugin The Bukkit plugin instance.
     */
    public MCEngineArtificialIntelligenceApiDBMongoDB(Plugin plugin) {
        this.plugin = plugin;

        String uri = plugin.getConfig().getString("database.mongodb.uri", "mongodb://localhost:27017");
        String dbName = plugin.getConfig().getString("database.mongodb.name", "mcengine_ai");
        String collectionName = plugin.getConfig().getString("database.mongodb.collection", "artificialintelligence");

        this.mongoClient = MongoClients.create(uri);
        this.database = mongoClient.getDatabase(dbName);
        this.collection = database.getCollection(collectionName);

        plugin.getLogger().info("Connected to MongoDB: " + uri + "/" + dbName + "." + collectionName);
    }

    /**
     * MongoDB does not use JDBC, so this returns null.
     *
     * @return Always returns null.
     */
    @Override
    public Connection getDBConnection() {
        return null;
    }

    /**
     * Sets or updates the encrypted token for a given player UUID and platform in MongoDB.
     *
     * @param playerUuid The UUID of the player.
     * @param platform   The AI platform.
     * @param token      The raw (unencrypted) token to store.
     */
    @Override
    public void setPlayerToken(String playerUuid, String platform, String token) {
        String encryptedToken = MCEngineArtificialIntelligenceApiUtilToken.encryptToken(token);

        Document filter = new Document("player_uuid", playerUuid).append("platform", platform);
        Document replacement = new Document("player_uuid", playerUuid)
                .append("platform", platform)
                .append("token", encryptedToken);

        try {
            collection.replaceOne(filter, replacement, new ReplaceOptions().upsert(true));
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to save token in MongoDB: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Retrieves the encrypted token for a player on a specific platform from MongoDB.
     *
     * @param playerUuid The UUID of the player.
     * @param platform   The AI platform.
     * @return The encrypted token or null if not found.
     */
    @Override
    public String getPlayerToken(String playerUuid, String platform) {
        try {
            Document result = collection.find(
                    Filters.and(
                            Filters.eq("player_uuid", playerUuid),
                            Filters.eq("platform", platform)
                    )
            ).first();

            if (result != null) {
                return result.getString("token");
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to retrieve token from MongoDB: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }
}
