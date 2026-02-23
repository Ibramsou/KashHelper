package fr.ibrakash.helper.item;

import fr.ibrakash.helper.utils.MaterialUtil;
import fr.ibrakash.helper.utils.TextUtil;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.components.CustomModelDataComponent;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@ConfigSerializable
public class ReplaceableItem {

    // TODO: Add custom item parser (ItemsAdder, Oraxen etc.)
    //private String customId = "";
    private String material = Material.PAPER.name();
    private int amount = 0;
    private boolean unbreakable = false;
    private String displayName = "";
    private List<String> lore = new ArrayList<>();
    private ItemModelComponents modelComponents = new ItemModelComponents();

    public ReplaceableItem() {}

    public ReplaceableItem material(Material material) {
        this.material = material.name();
        return this;
    }

    public ReplaceableItem amount(int amount) {
        this.amount = amount;
        return this;
    }

    public ReplaceableItem unbreakable(boolean unbreakable) {
        this.unbreakable = unbreakable;
        return this;
    }

    public ReplaceableItem displayName(String displayName) {
        this.displayName = displayName;
        return this;
    }

    public ReplaceableItem lore(List<String> lore) {
        this.lore = lore;
        return this;
    }

    public ReplaceableItem modelDataFloats(Float... floats) {
        this.modelComponents.setFloats(Arrays.asList(floats));
        return this;
    }

    public ReplaceableItem modelDataStrings(String... strings) {
        this.modelComponents.setStrings(Arrays.asList(strings));
        return this;
    }

    public ReplaceableItem modelDataFlags(Boolean... flags) {
        this.modelComponents.setFlags(Arrays.asList(flags));
        return this;
    }

    @SuppressWarnings("UnstableApiUsage")
    public ItemStack build(Object... replacers) {
        ItemStack itemStack = new ItemStack(MaterialUtil.parseOrThrow(this.material));
        itemStack.setAmount(this.amount);
        ItemMeta meta = itemStack.getItemMeta();
        meta.setUnbreakable(this.unbreakable);
        if (this.displayName != null && !displayName.isEmpty()) {
            meta.displayName(TextUtil.replacedComponent(this.displayName, replacers));
        }

        if (!this.lore.isEmpty()) {
            meta.lore(TextUtil.replacedComponents(this.lore, replacers));
        }

        // TODO: Add a wrapper for legacy versions
        if (!modelComponents.isEmpty()) {
            CustomModelDataComponent component = meta.getCustomModelDataComponent();
            component.setFloats(this.modelComponents.getFloats());
            component.setStrings(this.modelComponents.getStrings());
            component.setFlags(this.modelComponents.getFlags());
            meta.setCustomModelDataComponent(component);
        }

        itemStack.setItemMeta(meta);
        return itemStack;
    }
}
