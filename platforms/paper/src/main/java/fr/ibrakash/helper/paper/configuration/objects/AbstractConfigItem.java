package fr.ibrakash.helper.paper.configuration.objects;

import fr.ibrakash.helper.paper.item.AbstractItemReplacer;
import fr.ibrakash.helper.paper.item.ItemUtil;
import fr.ibrakash.helper.paper.item.parser.CustomItemParser;
import fr.ibrakash.helper.paper.item.replacer.ItemReplacer;
import fr.ibrakash.helper.paper.text.PaperTextReplacer;
import fr.ibrakash.helper.paper.utils.ItemModelComponents;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.components.CustomModelDataComponent;
import org.bukkit.persistence.PersistentDataContainer;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.UnaryOperator;

public class AbstractConfigItem {

    protected String item = Material.PAPER.name();
    protected int amount = 1;
    protected boolean unbreakable = false;
    protected String displayName = "";
    protected List<String> lore = new ArrayList<>();
    protected ItemModelComponents modelComponents = new ItemModelComponents();
    protected boolean glow = false;

    private transient boolean vanillaItem = false;

    public ItemBuilder builder() {
        return new ItemBuilder(this);
    }

    public ItemStack build() {
        return this.builder().build();
    }

    public ItemStack build(PaperTextReplacer textReplacer) {
        return this.builder().textReplacer(textReplacer).build();
    }

    public ItemStack build(PaperTextReplacer textReplacer, ItemReplacer itemReplacer) {
        return this.builder().textReplacer(textReplacer).itemReplacer(itemReplacer).build();
    }

    public String getItem() {
        return item;
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

    public void setItem(String item) {
        this.item = item;
    }

    public boolean isGlow() {
        return glow;
    }

    public void setGlow(boolean glow) {
        this.glow = glow;
    }

    protected void copyValues(AbstractConfigItem from, AbstractConfigItem to) {
        to.item = from.item;
        to.amount = from.amount;
        to.unbreakable = from.unbreakable;
        to.displayName = from.displayName;
        to.lore = from.lore;
        to.modelComponents = from.modelComponents;
    }

    public static class ItemBuilder {

        private static final PaperTextReplacer DEFAULT_TEXT_REPLACER = PaperTextReplacer.create();

        private final AbstractConfigItem config;

        private PaperTextReplacer textReplacer = DEFAULT_TEXT_REPLACER;
        private UnaryOperator<ItemStack> itemCreator;
        private Consumer<PersistentDataContainer> dataSetup;

        public ItemBuilder(AbstractConfigItem config) {
            this.config = config;
        }

        public ItemBuilder textReplacer(PaperTextReplacer textReplacer) {
            this.textReplacer = textReplacer;
            return this;
        }

        public ItemBuilder itemReplacer(ItemReplacer itemReplacer) {
            this.itemCreator = (itemStack) -> itemReplacer.createItemStack(null, config.item, itemStack);
            return this;
        }

        public <V> ItemBuilder itemReplacer(AbstractItemReplacer<V> itemReplacer, V value) {
            this.itemCreator = (itemStack) -> itemReplacer.createItemStack(value, config.item, itemStack);
            return this;
        }

        public ItemBuilder dataSetup(Consumer<PersistentDataContainer> dataSetup) {
            this.dataSetup = dataSetup;
            return this;
        }

        @SuppressWarnings("UnstableApiUsage")
        public ItemStack build() {
            ItemStack customItem = this.config.vanillaItem ? null : CustomItemParser.byId(this.config.item);
            if (customItem == null) this.config.vanillaItem = true;

            // Apply item stack replacer
            ItemStack itemStack = this.itemCreator == null ? ItemReplacer.create().createItemStack(null, this.config.item, customItem) :
                    this.itemCreator.apply(customItem);

            if (this.config.amount == 0) throw new IllegalArgumentException("ItemStack amount must be over 0");
            ItemMeta meta = itemStack.getItemMeta();
            meta.setUnbreakable(this.config.unbreakable);
            if (this.config.displayName != null && !this.config.displayName.isEmpty()) {
                meta.displayName(textReplacer.deserializeItemName(this.config.displayName));
            }

            if (!this.config.lore.isEmpty()) {
                meta.lore(textReplacer.deserializeItemLore(this.config.lore));
            }

            // TODO: Add a wrapper for legacy versions
            if (!this.config.modelComponents.isEmpty()) {
                CustomModelDataComponent component = meta.getCustomModelDataComponent();
                component.setFloats(this.config.modelComponents.getFloats());
                component.setStrings(this.config.modelComponents.getStrings());
                component.setFlags(this.config.modelComponents.getFlags());
                meta.setCustomModelDataComponent(component);
            }

            if (this.dataSetup != null) {
                this.dataSetup.accept(meta.getPersistentDataContainer());
            }

            itemStack.setItemMeta(meta);

            if (this.config.glow) {
                ItemUtil.setGlow(itemStack, true);
            }
            return itemStack;
        }
    }
}
