package io.github.mcengine.extension.addon.artificialintelligence.chatbot.util.db;

import io.github.mcengine.api.core.extension.logger.MCEngineExtensionLogger;

import java.sql.*;
import java.util.UUID;

/**
 * MySQL implementation of {@link ChatBotDB}.
 */
public class ChatBotDBMySQL implements ChatBotDB {

    /** Active JDBC connection supplied by the AI module. */
    private final Connection conn;

    /** Logger for diagnostics. */
    private final MCEngineExtensionLogger logger;

    /**
     * Constructs the DB helper.
     *
     * @param conn   JDBC connection
     * @param logger logger wrapper
     */
    public ChatBotDBMySQL(Connection conn, MCEngineExtensionLogger logger) {
        this.conn = conn;
        this.logger = logger;
    }

    @Override
    public void ensureSchema() {
        final String sql = """
            CREATE TABLE IF NOT EXISTS artificialintelligence_chatbot_mail (
                player_uuid VARCHAR(36) PRIMARY KEY,
                email TEXT NOT NULL
            ) ENGINE=InnoDB;
            """;
        try (Statement st = conn.createStatement()) {
            st.executeUpdate(sql);
            if (logger != null) logger.info("[ChatBotDB] MySQL schema ensured.");
        } catch (SQLException e) {
            if (logger != null) logger.warning("[ChatBotDB] MySQL schema creation failed: " + e.getMessage());
        }
    }

    @Override
    public String getPlayerEmail(UUID playerId) {
        final String sql = "SELECT email FROM artificialintelligence_chatbot_mail WHERE player_uuid = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, playerId.toString());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getString("email");
            }
        } catch (SQLException e) {
            if (logger != null) logger.warning("[ChatBotDB] MySQL get email failed: " + e.getMessage());
        }
        return null;
    }

    @Override
    public boolean setPlayerEmail(UUID playerId, String email) {
        if (email == null || !email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            if (logger != null) logger.warning("Rejected invalid email for " + playerId + ": " + email);
            return false;
        }
        final String sql = """
            INSERT INTO artificialintelligence_chatbot_mail (player_uuid, email)
            VALUES (?, ?)
            ON DUPLICATE KEY UPDATE email = VALUES(email);
            """;
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, playerId.toString());
            stmt.setString(2, email);
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            if (logger != null) logger.warning("[ChatBotDB] MySQL set email failed: " + e.getMessage());
            return false;
        }
    }
}
