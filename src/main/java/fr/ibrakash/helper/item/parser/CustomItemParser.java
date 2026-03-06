package fr.ibrakash.helper.item.parser;

import fr.ibrakash.helper.item.parser.type.ItemsAdderParser;
import fr.ibrakash.helper.item.parser.type.NexoParser;
import fr.ibrakash.helper.item.parser.type.OraxenParser;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

/**
 * Interface for parsing custom items from third-party plugins
 * Supports ItemsAdder, Oraxen, and Nexo
 */
public interface CustomItemParser {

    Map<String, CustomItemParser> CUSTOM_PARSERS = Map.of(
            "oraxen", OraxenParser.getInstance(),
            "nexo", NexoParser.getInstance(),
            "ia", ItemsAdderParser.getInstance());

    static ItemStack byId(String customId) {
        if (customId.isEmpty()) {
            return null;
        }

        if (!customId.contains(":")) return null;
        String[] split = customId.split(":", 2);
        String key = split[0].toLowerCase();
        String id = split[1];

        CustomItemParser customItemParser = CUSTOM_PARSERS.get(key);
        if (customItemParser == null) return null;
        if (!customItemParser.supported()) return null;

        return customItemParser.parseCustomItem(id);
    }

    /**
     * Attempts to parse a custom item from the given ID
     * @param customId the custom item identifier
     * @return ItemStack if found, null otherwise
     */
    ItemStack parseCustomItem(String customId);

    boolean supported();

}

