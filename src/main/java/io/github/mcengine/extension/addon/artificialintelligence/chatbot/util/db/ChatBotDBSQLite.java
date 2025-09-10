package io.github.mcengine.extension.addon.artificialintelligence.chatbot.util.db;

import io.github.mcengine.api.core.extension.logger.MCEngineExtensionLogger;
import io.github.mcengine.common.artificialintelligence.MCEngineArtificialIntelligenceCommon;

import java.util.UUID;

/**
 * SQLite implementation of {@link ChatBotDB}.
 */
public class ChatBotDBSQLite implements ChatBotDB {

    /** Logger for diagnostics. */
    private final MCEngineExtensionLogger logger;

    /**
     * Constructs the DB helper.
     *
     * @param logger logger wrapper
     */
    public ChatBotDBSQLite(MCEngineExtensionLogger logger) {
        this.logger = logger;
    }

    @Override
    public void ensureSchema() {
        final String sql = """
            CREATE TABLE IF NOT EXISTS artificialintelligence_chatbot_mail (
                player_uuid VARCHAR(36) PRIMARY KEY,
                email TEXT NOT NULL
            );
            """;
        try {
            MCEngineArtificialIntelligenceCommon.getApi().executeQuery(sql);
            if (logger != null) logger.info("[ChatBotDB] SQLite schema ensured.");
        } catch (Exception e) {
            if (logger != null) logger.warning("[ChatBotDB] SQLite schema creation failed: " + e.getMessage());
        }
    }

    @Override
    public String getPlayerEmail(UUID playerId) {
        final String sql = "SELECT email FROM artificialintelligence_chatbot_mail " +
                "WHERE player_uuid = '" + escape(playerId.toString()) + "' LIMIT 1;";
        try {
            return MCEngineArtificialIntelligenceCommon.getApi().getValue(sql, String.class);
        } catch (Exception e) {
            if (logger != null) logger.warning("[ChatBotDB] SQLite get email failed: " + e.getMessage());
            return null;
        }
    }

    @Override
       public boolean setPlayerEmail(UUID playerId, String email) {
        if (email == null || !email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            if (logger != null) logger.warning("Rejected invalid email for " + playerId + ": " + email);
            return false;
        }
        // Delete+insert to avoid dialect-specific UPSERT syntax.
        final String del = "DELETE FROM artificialintelligence_chatbot_mail " +
                "WHERE player_uuid = '" + escape(playerId.toString()) + "';";
        final String ins = "INSERT INTO artificialintelligence_chatbot_mail (player_uuid, email) VALUES (" +
                "'" + escape(playerId.toString()) + "', '" + escape(email) + "');";
        try {
            MCEngineArtificialIntelligenceCommon.getApi().executeQuery(del);
            MCEngineArtificialIntelligenceCommon.getApi().executeQuery(ins);
            return true;
        } catch (Exception e) {
            if (logger != null) logger.warning("[ChatBotDB] SQLite set email failed: " + e.getMessage());
            return false;
        }
    }

    /** Minimal SQL string escaper for single quotes. */
    private static String escape(String s) {
        return s == null ? "" : s.replace("'", "''");
    }
}
