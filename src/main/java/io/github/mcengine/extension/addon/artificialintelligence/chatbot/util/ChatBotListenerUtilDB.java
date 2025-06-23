package io.github.mcengine.extension.addon.artificialintelligence.chatbot.util;

import io.github.mcengine.api.artificialintelligence.MCEngineArtificialIntelligenceApi;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Database utility class for chatbot email functionality.
 * <p>
 * Handles table creation, email insertion, and retrieval.
 */
public class ChatBotListenerUtilDB {

    /**
     * Initializes the chatbot mail table if it does not exist.
     * Should be called on plugin startup.
     */
    public static void initialize() {
        try (Connection conn = MCEngineArtificialIntelligenceApi.getApi().getDBConnection()) {
            String sql = """
                CREATE TABLE IF NOT EXISTS artificialintelligence_chatbot_mail (
                    player_uuid TEXT PRIMARY KEY,
                    email TEXT NOT NULL
                );
            """;
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.execute();
            }
        } catch (SQLException e) {
            MCEngineArtificialIntelligenceApi.getApi().getPlugin().getLogger()
                .severe("[ChatBot] Failed to initialize mail table: " + e.getMessage());
        }
    }

    /**
     * Retrieves the email address associated with the given player.
     *
     * @param player The player.
     * @return The email address if found, or {@code null}.
     */
    public static String getPlayerEmail(Player player) {
        try (Connection conn = MCEngineArtificialIntelligenceApi.getApi().getDBConnection()) {
            String sql = "SELECT email FROM artificialintelligence_chatbot_mail WHERE player_uuid = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, player.getUniqueId().toString());
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return rs.getString("email");
                    }
                }
            }
        } catch (SQLException e) {
            MCEngineArtificialIntelligenceApi.getApi().getPlugin().getLogger()
                .severe("[ChatBot] Failed to retrieve email for player " + player.getName() + ": " + e.getMessage());
        }
        return null;
    }

    /**
     * Sets or updates the email address for the given player.
     * The email must be a valid format and stored as text.
     *
     * @param player The player whose email is being set.
     * @param email  The email address to save.
     */
    public static void setPlayerEmail(Player player, String email) {
        if (email == null || !email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            player.sendMessage("§c[ChatBot] Invalid email format: " + email);
            return;
        }

        try (Connection conn = MCEngineArtificialIntelligenceApi.getApi().getDBConnection()) {
            String sql = """
                INSERT INTO artificialintelligence_chatbot_mail (player_uuid, email)
                VALUES (?, ?)
                ON CONFLICT(player_uuid) DO UPDATE SET email = excluded.email
            """;
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, player.getUniqueId().toString());
                stmt.setString(2, email);
                stmt.executeUpdate();
                player.sendMessage("§a[ChatBot] Your email has been successfully saved!");
            }
        } catch (SQLException e) {
            MCEngineArtificialIntelligenceApi.getApi().getPlugin().getLogger()
                .severe("[ChatBot] Failed to save email for player " + player.getName() + ": " + e.getMessage());
        }
    }
}
