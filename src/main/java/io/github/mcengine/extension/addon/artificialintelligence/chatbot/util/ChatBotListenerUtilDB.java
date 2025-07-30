package io.github.mcengine.extension.addon.artificialintelligence.chatbot.util;

import io.github.mcengine.api.core.extension.logger.MCEngineExtensionLogger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

/**
 * Handles chatbot email table creation and CRUD operations.
 * This class is used to store and manage email addresses linked to player UUIDs.
 */
public class ChatBotListenerUtilDB {

    private final Connection conn;
    private final MCEngineExtensionLogger logger;

    /**
     * Constructs a new database utility for chatbot email storage.
     *
     * @param conn   The database connection to use.
     * @param logger The logger for reporting errors or info.
     */
    public ChatBotListenerUtilDB(Connection conn, MCEngineExtensionLogger logger) {
        this.conn = conn;
        this.logger = logger;
        createTable();
    }

    /**
     * Creates the chatbot mail table if it doesn't already exist.
     */
    public void createTable() {
        String sql = """
            CREATE TABLE IF NOT EXISTS artificialintelligence_chatbot_mail (
                player_uuid TEXT PRIMARY KEY,
                email TEXT NOT NULL
            );
        """;
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.execute();
            logger.info("ChatBot email table created or already exists.");
        } catch (SQLException e) {
            logger.warning("Failed to create email table: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Retrieves the email address associated with the given player UUID.
     *
     * @param playerId The UUID of the player.
     * @return The email address, or {@code null} if not found.
     */
    public String getPlayerEmail(UUID playerId) {
        String sql = "SELECT email FROM artificialintelligence_chatbot_mail WHERE player_uuid = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, playerId.toString());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("email");
                }
            }
        } catch (SQLException e) {
            logger.warning("Failed to retrieve email for player " + playerId + ": " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Inserts or updates the email address for the given player UUID.
     *
     * @param playerId The player's UUID.
     * @param email    The email address to associate.
     * @return True if the email was saved successfully; false otherwise.
     */
    public boolean setPlayerEmail(UUID playerId, String email) {
        if (email == null || !email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            logger.warning("Rejected invalid email format for player " + playerId + ": " + email);
            return false;
        }

        String sql = """
            INSERT INTO artificialintelligence_chatbot_mail (player_uuid, email)
            VALUES (?, ?)
            ON CONFLICT(player_uuid) DO UPDATE SET email = excluded.email;
        """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, playerId.toString());
            stmt.setString(2, email);
            stmt.executeUpdate();
            logger.info("Email saved for player " + playerId);
            return true;
        } catch (SQLException e) {
            logger.warning("Failed to save email for player " + playerId + ": " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}
