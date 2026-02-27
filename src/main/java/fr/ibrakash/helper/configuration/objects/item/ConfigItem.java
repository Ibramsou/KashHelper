package fr.ibrakash.helper.configuration.objects.item;

import fr.ibrakash.helper.configuration.objects.AbstractConfigItem;
import org.bukkit.Material;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;

import java.util.Arrays;
import java.util.List;

@ConfigSerializable
public class ConfigItem extends AbstractConfigItem {

    public ConfigItem() {}

    public ConfigItem material(String material) {
        this.material = material;
        return this;
    }

    public ConfigItem material(Material material) {
        this.material = material.name();
        return this;
    }

    public ConfigItem amount(int amount) {
        this.amount = amount;
        return this;
    }

    public ConfigItem unbreakable(boolean unbreakable) {
        this.unbreakable = unbreakable;
        return this;
    }

    public ConfigItem displayName(String displayName) {
        this.displayName = displayName;
        return this;
    }

    public ConfigItem lore(List<String> lore) {
        this.lore = lore;
        return this;
    }

    public ConfigItem modelDataFloats(Float... floats) {
        this.modelComponents.setFloats(Arrays.asList(floats));
        return this;
    }

    public ConfigItem modelDataStrings(String... strings) {
        this.modelComponents.setStrings(Arrays.asList(strings));
        return this;
    }

    public ConfigItem modelDataFlags(Boolean... flags) {
        this.modelComponents.setFlags(Arrays.asList(flags));
        return this;
    }

    @Override
    public ConfigItem clone() {
        ConfigItem guiItem = new ConfigItem();
        super.copyValues(this, guiItem);
        return guiItem;
    }
}
