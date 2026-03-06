package fr.ibrakash.helper.item.parser;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import fr.ibrakash.helper.item.parser.type.ItemsAdderParser;
import fr.ibrakash.helper.item.parser.type.NexoParser;
import fr.ibrakash.helper.item.parser.type.OraxenParser;
import org.bukkit.inventory.ItemStack;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

/**
 * Interface for parsing custom items from third-party plugins
 * Supports ItemsAdder, Oraxen, and Nexo
 */
public interface CustomItemParser {

    List<CustomItemParser> CUSTOM_PARSERS = Stream.of(
            OraxenParser.getInstance(),
            NexoParser.getInstance(),
            ItemsAdderParser.getInstance()
    ).sorted(Comparator.comparingInt(CustomItemParser::getPriority).reversed()).toList();

    static ItemStack byId(String customId) {
        if (customId.isEmpty()) {
            return null;
        }

        // Try parsers in order of priority
        for (CustomItemParser parser : CUSTOM_PARSERS) {
            if (parser.supports(customId)) {
                ItemStack itemStack = parser.parseCustomItem(customId);
                if (itemStack != null) {
                    return itemStack;
                }
            }
        }
        return null;
    }

    /**
     * Attempts to parse a custom item from the given ID
     * @param customId the custom item identifier
     * @return ItemStack if found, null otherwise
     */
    ItemStack parseCustomItem(String customId);

    /**
     * Checks if this parser supports the given custom item ID
     * @param customId the custom item identifier
     * @return true if this parser can handle it
     */
    boolean supports(String customId);

    /**
     * Gets the priority of this parser (higher = checked first)
     * @return priority level
     */
    default int getPriority() {
        return 0;
    }
}

