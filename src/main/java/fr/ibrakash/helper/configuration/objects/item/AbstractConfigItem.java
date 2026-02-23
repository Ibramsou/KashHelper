package fr.ibrakash.helper.configuration.objects.item;

import fr.ibrakash.helper.utils.MaterialUtil;
import fr.ibrakash.helper.utils.TextUtil;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.components.CustomModelDataComponent;

import java.util.ArrayList;
import java.util.List;

public class AbstractConfigItem {

    // TODO: Add custom item parser (ItemsAdder, Oraxen etc.)
    //private String customId = "";
    protected String material = Material.PAPER.name();
    protected int amount = 1;
    protected boolean unbreakable = false;
    protected String displayName = "";
    protected List<String> lore = new ArrayList<>();
    protected ItemModelComponents modelComponents = new ItemModelComponents();

    @SuppressWarnings("UnstableApiUsage")
    public ItemStack build(Object... replacers) {
        ItemStack itemStack = new ItemStack(MaterialUtil.parseOrThrow(this.material));
        if (this.amount == 0) throw new IllegalArgumentException("ItemStack amount must be over 0");
        ItemMeta meta = itemStack.getItemMeta();
        meta.setUnbreakable(this.unbreakable);
        if (this.displayName != null && !displayName.isEmpty()) {
            meta.displayName(TextUtil.replacedItemComponent(this.displayName, replacers));
        }

        if (!this.lore.isEmpty()) {
            meta.lore(TextUtil.replacedItemComponents(this.lore, replacers));
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

    public String getMaterial() {
        return material;
    }

    public int getAmount() {
        return amount;
    }

    public boolean isUnbreakable() {
        return unbreakable;
    }

    public String getDisplayName() {
        return displayName;
    }

    public List<String> getLore() {
        return lore;
    }

    public ItemModelComponents getModelComponents() {
        return modelComponents;
    }
}
