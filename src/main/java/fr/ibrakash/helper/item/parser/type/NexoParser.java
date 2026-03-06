package fr.ibrakash.helper.item.parser.type;

import com.nexomc.nexo.items.ItemBuilder;
import fr.ibrakash.helper.item.parser.CustomItemParser;
import org.bukkit.inventory.ItemStack;

/**
 * Custom item parser for Nexo plugin
 */
public class NexoParser implements CustomItemParser {

    private static NexoParser instance;

    private NexoParser() {}

    public static NexoParser getInstance() {
        if (instance == null) {
            instance = new NexoParser();
        }
        return instance;
    }

    @Override
    public ItemStack parseCustomItem(String customId) {
        try {
            return com.nexomc.nexo.api.NexoItems.optionalItemFromId(customId)
                    .map(ItemBuilder::build).orElse( null);
        } catch (Exception e) {}
        return null;
    }

    @Override
    public boolean supported() {
        try {
            Class.forName("com.nexomc.nexo.items.ItemRegistry");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}

