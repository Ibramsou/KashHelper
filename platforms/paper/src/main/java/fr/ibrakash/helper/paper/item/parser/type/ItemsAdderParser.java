package fr.ibrakash.helper.paper.item.parser.type;

import fr.ibrakash.helper.paper.item.parser.CustomItemParser;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Method;

/**
 * Custom item parser for ItemsAdder plugin.
 * Uses reflection to avoid ClassNotFoundException when loaded by a classloader
 * that cannot see ItemsAdder's plugin classes.
 */
public class ItemsAdderParser implements CustomItemParser {

    private static ItemsAdderParser instance;

    private Method getInstanceMethod;
    private Method getItemStackMethod;
    private boolean initialized;

    private ItemsAdderParser() {}

    public static ItemsAdderParser getInstance() {
        if (instance == null) {
            instance = new ItemsAdderParser();
        }
        return instance;
    }

    private boolean init() {
        if (initialized) return getInstanceMethod != null;
        initialized = true;
        try {
            ClassLoader cl = Bukkit.getPluginManager().getPlugin("ItemsAdder").getClass().getClassLoader();
            Class<?> customStackClass = Class.forName("dev.lone.itemsadder.api.CustomStack", true, cl);
            getInstanceMethod = customStackClass.getMethod("getInstance", String.class);
            getItemStackMethod = customStackClass.getMethod("getItemStack");
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    @Override
    public ItemStack parseCustomItem(String customId) {
        try {
            if (!init()) return null;
            Object customStack = getInstanceMethod.invoke(null, customId);
            if (customStack == null) return null;
            return (ItemStack) getItemStackMethod.invoke(customStack);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public boolean supported() {
        return Bukkit.getPluginManager().isPluginEnabled("ItemsAdder");
    }
}

