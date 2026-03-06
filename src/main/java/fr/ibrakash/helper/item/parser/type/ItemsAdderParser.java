package fr.ibrakash.helper.item.parser.type;

import fr.ibrakash.helper.item.parser.CustomItemParser;
import org.bukkit.inventory.ItemStack;

/**
 * Custom item parser for ItemsAdder plugin
 */
public class ItemsAdderParser implements CustomItemParser {

    private static ItemsAdderParser instance;

    private ItemsAdderParser() {}

    public static ItemsAdderParser getInstance() {
        if (instance == null) {
            instance = new ItemsAdderParser();
        }
        return instance;
    }

    @Override
    public ItemStack parseCustomItem(String customId) {
        try {
            dev.lone.itemsadder.api.CustomStack customStack = dev.lone.itemsadder.api.CustomStack.getInstance(customId);
            if (customStack == null) return null;
            return customStack.getItemStack();
        } catch (Exception e) {}
        return null;
    }

    @Override
    public boolean supported() {
        try {
            Class.forName("dev.lone.itemsadder.api.CustomStack");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}

