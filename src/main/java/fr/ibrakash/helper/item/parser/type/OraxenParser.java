package fr.ibrakash.helper.item.parser.type;

import fr.ibrakash.helper.item.parser.CustomItemParser;
import io.th0rgal.oraxen.items.ItemBuilder;
import org.bukkit.inventory.ItemStack;

/**
 * Custom item parser for Oraxen plugin
 */
public class OraxenParser implements CustomItemParser {

    private static OraxenParser instance;

    private OraxenParser() {}

    public static OraxenParser getInstance() {
        if (instance == null) {
            instance = new OraxenParser();
        }
        return instance;
    }

    @Override
    public ItemStack parseCustomItem(String customId) {
        try {
            return io.th0rgal.oraxen.api.OraxenItems.getOptionalItemById(customId)
                    .map(ItemBuilder::build).orElse(null);
        } catch (Exception e) {}
        return null;
    }

    @Override
    public boolean supported() {
        try {
            Class.forName("io.th0rgal.oraxen.items.OraxenItems");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}

