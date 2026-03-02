package fr.ibrakash.helper.item.replacer;

import fr.ibrakash.helper.item.AbstractItemReplacer;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.function.Supplier;

public class ItemReplacer extends AbstractItemReplacer<Void> {

    private static final ItemReplacer EMPTY = new ItemReplacer();

    public static ItemReplacer create() {
        return new ItemReplacer();
    }

    public static ItemReplacer empty() {
        return EMPTY;
    }

    ItemReplacer() {}

    public ItemReplacer material(String key, Supplier<Material> supplier) {
        this.addMaterial(key, v -> supplier.get());
        return this;
    }

    public ItemReplacer itemStack(String key, Supplier<ItemStack> supplier) {
        this.addItemStack(key, v -> supplier.get());
        return this;
    }
}
