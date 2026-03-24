package fr.ibrakash.helper.paper.item.parser.type;

import fr.ibrakash.helper.paper.item.parser.CustomItemParser;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Method;
import java.util.Optional;

/**
 * Custom item parser for Nexo plugin.
 * Uses reflection to avoid ClassNotFoundException when loaded by a classloader
 * that cannot see Nexo's plugin classes (e.g. Paper's library classloader).
 */
public class NexoParser implements CustomItemParser {

    private static NexoParser instance;

    private Method optionalItemFromIdMethod;
    private Method buildMethod;
    private boolean initialized;

    private NexoParser() {}

    public static NexoParser getInstance() {
        if (instance == null) {
            instance = new NexoParser();
        }
        return instance;
    }

    private boolean init() {
        if (initialized) return optionalItemFromIdMethod != null;
        initialized = true;
        try {
            ClassLoader cl = Bukkit.getPluginManager().getPlugin("Nexo").getClass().getClassLoader();
            Class<?> nexoItemsClass = Class.forName("com.nexomc.nexo.api.NexoItems", true, cl);
            Class<?> itemBuilderClass = Class.forName("com.nexomc.nexo.items.ItemBuilder", true, cl);
            optionalItemFromIdMethod = nexoItemsClass.getMethod("optionalItemFromId", String.class);
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
            Optional<?> optional = (Optional<?>) optionalItemFromIdMethod.invoke(null, customId);
            if (optional.isEmpty()) return null;
            return (ItemStack) buildMethod.invoke(optional.get());
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public boolean supported() {
        return Bukkit.getPluginManager().isPluginEnabled("Nexo");
    }
}

