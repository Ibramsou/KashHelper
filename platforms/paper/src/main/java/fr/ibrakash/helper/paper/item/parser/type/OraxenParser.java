package fr.ibrakash.helper.paper.item.parser.type;

import fr.ibrakash.helper.paper.item.parser.CustomItemParser;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Method;
import java.util.Optional;

/**
 * Custom item parser for Oraxen plugin.
 * Uses reflection to avoid ClassNotFoundException when loaded by a classloader
 * that cannot see Oraxen's plugin classes.
 */
public class OraxenParser implements CustomItemParser {

    private static OraxenParser instance;

    private Method getOptionalItemByIdMethod;
    private Method buildMethod;
    private boolean initialized;

    private OraxenParser() {}

    public static OraxenParser getInstance() {
        if (instance == null) {
            instance = new OraxenParser();
        }
        return instance;
    }

    private boolean init() {
        if (initialized) return getOptionalItemByIdMethod != null;
        initialized = true;
        try {
            ClassLoader cl = Bukkit.getPluginManager().getPlugin("Oraxen").getClass().getClassLoader();
            Class<?> oraxenItemsClass = Class.forName("io.th0rgal.oraxen.api.OraxenItems", true, cl);
            Class<?> itemBuilderClass = Class.forName("io.th0rgal.oraxen.items.ItemBuilder", true, cl);
            getOptionalItemByIdMethod = oraxenItemsClass.getMethod("getOptionalItemById", String.class);
            buildMethod = itemBuilderClass.getMethod("build");
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    @Override
    @SuppressWarnings("unchecked")
    public ItemStack parseCustomItem(String customId) {
        try {
            if (!init()) return null;
            Optional<?> optional = (Optional<?>) getOptionalItemByIdMethod.invoke(null, customId);
            if (optional.isEmpty()) return null;
            return (ItemStack) buildMethod.invoke(optional.get());
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public boolean supported() {
        return Bukkit.getPluginManager().isPluginEnabled("Oraxen");
    }
}

