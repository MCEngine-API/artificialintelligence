package io.github.mcengine.api.artificialintelligence.function.calling.util;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.StringJoiner;

/**
 * Utility class for extracting and formatting details about items held by or in the inventory of a player.
 */
public class FunctionCallingItem {

    /**
     * Returns a formatted string containing details of the item in the player's main hand.
     *
     * @param player the player whose main hand item will be inspected
     * @return a string with the item details or a message if no item is held
     */
    public static String getItemInHandDetails(Player player) {
        ItemStack item = player.getInventory().getItemInMainHand();
        return item != null ? formatItemDetails(item) : "No item in hand.";
    }

    /**
     * Returns a formatted string containing details of all non-null items in the player's inventory.
     *
     * @param player the player whose inventory will be inspected
     * @return a string listing item details or a message if inventory is empty
     */
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

    /**
     * Constructs a string with detailed information about a specific item.
     *
     * @param item the item to be formatted
     * @return a string containing type, amount, name, lore, and model data if available
     */
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
