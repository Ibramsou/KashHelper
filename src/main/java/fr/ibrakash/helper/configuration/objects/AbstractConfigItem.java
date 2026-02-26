package fr.ibrakash.helper.configuration.objects;

import fr.ibrakash.helper.utils.ItemModelComponents;
import fr.ibrakash.helper.utils.MaterialUtil;
import fr.ibrakash.helper.utils.PlayerProfileCache;
import fr.ibrakash.helper.utils.TextUtil;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.inventory.meta.components.CustomModelDataComponent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class AbstractConfigItem {

    // TODO: Add custom item parser (ItemsAdder, Oraxen etc.)
    //private String customId = "";
    protected String material = Material.PAPER.name();
    protected int amount = 1;
    protected boolean unbreakable = false;
    protected String displayName = "";
    protected List<String> lore = new ArrayList<>();
    protected ItemModelComponents modelComponents = new ItemModelComponents();

    public ItemStack build(Object... replacers) {
        return build(Arrays.asList(replacers));
    }

    @SuppressWarnings("UnstableApiUsage")
    public ItemStack build(List<Object> replacers) {
        ItemStack itemStack;
        if (this.material.contains(":")) {
            String[] split = this.material.split(":");
            String type =  split[0];
            String input = split[1];

            itemStack = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta meta = (SkullMeta) itemStack;
            switch (type) {
                case "skin_texture" -> meta.setPlayerProfile(PlayerProfileCache.fetchTextureSkin(input));
                case "uuid_skin" -> meta.setPlayerProfile(PlayerProfileCache.fetchTextureSkin(UUID.fromString(input)).join());
                case "username_skin" -> meta.setPlayerProfile(PlayerProfileCache.fetchUsernameSkin(input).join());
                default -> throw new IllegalArgumentException("Invalid skin type: " + material);
            }
            itemStack.setItemMeta(meta);
        } else if (this.material.startsWith("%") && this.material.endsWith("%")) {
            itemStack = new ItemStack(MaterialUtil.parseOrThrow(TextUtil.replaced(this.material, replacers)));
        } else {
            itemStack = new ItemStack(MaterialUtil.parseOrThrow(this.material));
        }
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
