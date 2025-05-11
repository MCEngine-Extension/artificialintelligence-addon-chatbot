package io.github.mcengine.addon.artificialintelligence.chatbot.util;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages AI chat conversations for each player.
 * Tracks active conversations, waiting status, and session termination.
 */
public class ChatManager {

    /** Stores the conversation history for each player */
    private static final Map<UUID, StringBuilder> playerConversations = new ConcurrentHashMap<>();

    /** Tracks players currently in an active conversation */
    private static final Set<UUID> activePlayers = ConcurrentHashMap.newKeySet();

    /** Tracks players who are waiting for AI response */
    private static final Set<UUID> waitingPlayers = ConcurrentHashMap.newKeySet();

    /**
     * Starts a new conversation session for a player.
     * @param player The player starting a conversation.
     */
    public static void startConversation(Player player) {
        playerConversations.put(player.getUniqueId(), new StringBuilder());
    }

    /**
     * Appends a message to the player's conversation.
     * @param player The player to append the message for.
     * @param message The message to append.
     */
    public static void append(Player player, String message) {
        playerConversations
            .computeIfAbsent(player.getUniqueId(), k -> new StringBuilder())
            .append(message)
            .append("\n");
    }

    /**
     * Retrieves the entire conversation history of a player.
     * @param player The player whose conversation to retrieve.
     * @return The conversation history as a string.
     */
    public static String get(Player player) {
        return playerConversations.getOrDefault(player.getUniqueId(), new StringBuilder()).toString();
    }

    /**
     * Ends and removes a player's conversation session.
     * @param player The player whose session to end.
     */
    public static void end(Player player) {
        playerConversations.remove(player.getUniqueId());
    }

    /**
     * Marks a player as actively chatting with AI.
     * @param player The player to activate.
     */
    public static void activate(Player player) {
        activePlayers.add(player.getUniqueId());
    }

    /**
     * Removes a player from the active chat list.
     * @param player The player to deactivate.
     */
    public static void deactivate(Player player) {
        activePlayers.remove(player.getUniqueId());
    }

    /**
     * Checks if a player is currently in an active conversation.
     * @param player The player to check.
     * @return True if the player is active, false otherwise.
     */
    public static boolean isActive(Player player) {
        return activePlayers.contains(player.getUniqueId());
    }

    /**
     * Fully terminates a player's session, removing them from all tracking.
     * @param player The player to terminate.
     */
    public static void terminate(Player player) {
        end(player);
        deactivate(player);
        setWaiting(player, false);
    }

    /**
     * Checks if a player is waiting for a response from the AI.
     * @param player The player to check.
     * @return True if the player is waiting, false otherwise.
     */
    public static boolean isWaiting(Player player) {
        return waitingPlayers.contains(player.getUniqueId());
    }

    /**
     * Sets the waiting status of a player.
     * @param player The player whose status to set.
     * @param waiting True to mark as waiting, false to clear.
     */
    public static void setWaiting(Player player, boolean waiting) {
        if (waiting) {
            waitingPlayers.add(player.getUniqueId());
        } else {
            waitingPlayers.remove(player.getUniqueId());
        }
    }

    /**
     * Terminates all active conversations and notifies online players.
     * Used during plugin disable or reload.
     */
    public static void terminateAll() {
        for (UUID uuid : Set.copyOf(activePlayers)) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null && player.isOnline()) {
                terminate(player);
                player.sendMessage("§cYour AI session has ended due to the plugin being reloaded or disabled.");
            }
        }

        // Fallback cleanup
        playerConversations.clear();
        activePlayers.clear();
        waitingPlayers.clear();
    }
}
