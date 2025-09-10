package io.github.mcengine.extension.addon.artificialintelligence.chatbot.database;

import java.util.UUID;

/**
 * Abstraction for ChatBot database operations (multi-dialect support).
 *
 * <p>Implementations must manage a single table:</p>
 * <ul>
 *   <li><strong>artificialintelligence_chatbot_mail</strong>
 *       (player_uuid PK, email TEXT/VARCHAR NOT NULL)</li>
 * </ul>
 */
public interface ChatBotDB {

    /** Creates required tables if they don't already exist. */
    void ensureSchema();

    /**
     * Retrieves the email address associated with the given player.
     *
     * @param playerId player UUID
     * @return email or {@code null} if not set
     */
    String getPlayerEmail(UUID playerId);

    /**
     * Inserts or updates the email address for the given player.
     *
     * @param playerId player UUID
     * @param email    email address to set (validated by implementation)
     * @return {@code true} on success; {@code false} otherwise
     */
    boolean setPlayerEmail(UUID playerId, String email);
}
