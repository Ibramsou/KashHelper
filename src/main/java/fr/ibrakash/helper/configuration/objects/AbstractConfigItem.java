package fr.ibrakash.helper.configuration.objects;

import fr.ibrakash.helper.item.AbstractItemReplacer;
import fr.ibrakash.helper.item.ItemUtil;
import fr.ibrakash.helper.item.replacer.ItemReplacer;
import fr.ibrakash.helper.text.TextReplacer;
import fr.ibrakash.helper.utils.ItemModelComponents;
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
    protected boolean glow = false;

    public ItemStack build(TextReplacer textReplacer) {
        return this.build(textReplacer, ItemReplacer.empty());
    }

    public ItemStack build(TextReplacer textReplacer, ItemReplacer itemReplacer) {
        return this.build(textReplacer, itemReplacer, null);
    }

    @SuppressWarnings("UnstableApiUsage")
    public <V> ItemStack build(TextReplacer textReplacer, AbstractItemReplacer<V> itemReplacer, V value) {
        ItemStack itemStack = itemReplacer.createItemStack(value, this.material);
        if (this.amount == 0) throw new IllegalArgumentException("ItemStack amount must be over 0");
        ItemMeta meta = itemStack.getItemMeta();
        meta.setUnbreakable(this.unbreakable);
        if (this.displayName != null && !displayName.isEmpty()) {
            meta.displayName(textReplacer.deserializeItemName(this.displayName));
        }

        if (!this.lore.isEmpty()) {
            meta.lore(textReplacer.deserializeItemLore(this.lore));
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

        if (this.glow) {
            ItemUtil.setGlow(itemStack, true);
        }
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

    public void setMaterial(String material) {
        this.material = material;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public void setUnbreakable(boolean unbreakable) {
        this.unbreakable = unbreakable;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public void setLore(List<String> lore) {
        this.lore = lore;
    }

    public void setModelComponents(ItemModelComponents modelComponents) {
        this.modelComponents = modelComponents;
    }

    protected void copyValues(AbstractConfigItem from, AbstractConfigItem to) {
        to.material = from.material;
        to.amount = from.amount;
        to.unbreakable = from.unbreakable;
        to.displayName = from.displayName;
        to.lore = from.lore;
        to.modelComponents = from.modelComponents;
    }
}
