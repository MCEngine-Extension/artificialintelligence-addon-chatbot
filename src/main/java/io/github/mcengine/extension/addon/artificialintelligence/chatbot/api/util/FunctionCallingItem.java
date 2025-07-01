package io.github.mcengine.extension.addon.artificialintelligence.chatbot.api.util;

import io.github.mcengine.api.core.extension.addon.MCEngineAddOnLogger;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.StringJoiner;

public class FunctionCallingItem {

    public static void check(MCEngineAddOnLogger logger) {
        logger.info("Class: FunctionCallingItem is loadded.");
    }

    public static String getItemInHandDetails(Player player) {
        ItemStack item = player.getInventory().getItemInMainHand();
        return item != null ? formatItemDetails(item) : "No item in hand.";
    }

    public static String getPlayerInventoryDetails(Player player) {
        ItemStack[] contents = player.getInventory().getContents();
        StringJoiner joiner = new StringJoiner("\n");
        for (ItemStack item : contents) {
            if (item != null) {
                joiner.add(formatItemDetails(item));
            }
        }
        return joiner.length() > 0 ? joiner.toString() : "Inventory is empty.";
    }

    private static String formatItemDetails(ItemStack item) {
        StringBuilder sb = new StringBuilder();
        sb.append("Type: ").append(item.getType());
        sb.append(", Amount: ").append(item.getAmount());

        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            if (meta.hasDisplayName()) {
                sb.append(", Name: ").append(ChatColor.stripColor(meta.getDisplayName()));
            }
            if (meta.hasLore()) {
                sb.append(", Lore: ").append(String.join(" | ", meta.getLore()));
            }
            if (meta.hasCustomModelData()) {
                sb.append(", ModelData: ").append(meta.getCustomModelData());
            }
        }

        return sb.toString();
    }
}
