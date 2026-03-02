package fr.ibrakash.helper.item;

import fr.ibrakash.helper.material.MaterialUtil;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class AbstractItemReplacer<V> {

    private final Map<String, Function<V, ItemStack>> itemMap = new HashMap<>();

    protected void addMaterial(String key, Function<V, Material> function) {
        this.addItemStack(key, object -> new ItemStack(function.apply(object)));
    }

    protected void addItemStack(String key, Function<V, ItemStack> function) {
        this.itemMap.put(key, function);
    }

    public ItemStack createItemStack(V value, String item) {
        return ItemUtil.parseSkullItem(item).orElseGet(() -> {
            Function<V, ItemStack> function = this.itemMap.get(item);
            if (function == null) {
                return new ItemStack(MaterialUtil.parseOrThrow(item));
            } else {
                return function.apply(value);
            }
        }
        );
    }
}
