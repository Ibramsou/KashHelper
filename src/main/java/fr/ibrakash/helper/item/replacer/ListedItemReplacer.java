package fr.ibrakash.helper.item.replacer;

import fr.ibrakash.helper.item.AbstractItemReplacer;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.function.Function;

public class ListedItemReplacer<V> extends AbstractItemReplacer<V> {

    public static <V> ListedItemReplacer<V> create() {
        return new ListedItemReplacer<>();
    }

    ListedItemReplacer() {}

    public ListedItemReplacer<V> material(String key, Function<V, Material> function) {
        this.addMaterial(key, function);
        return this;
    }

    public ListedItemReplacer<V> itemStack(String key, Function<V, ItemStack> function) {
        this.addItemStack(key, function);
        return this;
    }
}
