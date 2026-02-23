package fr.ibrakash.helper.configuration.objects.item.impl;

import fr.ibrakash.helper.configuration.objects.item.AbstractConfigItem;
import org.bukkit.Material;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@ConfigSerializable
public class ConfigItemSlot extends AbstractConfigItem {

    private List<Integer> slots = new ArrayList<>();

    public ConfigItemSlot() {}

    public ConfigItemSlot slots(Integer... slots) {
        this.slots = List.of(slots);
        return this;
    }

    public ConfigItemSlot material(Material material) {
        this.material = material.name();
        return this;
    }

    public ConfigItemSlot amount(int amount) {
        this.amount = amount;
        return this;
    }

    public ConfigItemSlot unbreakable(boolean unbreakable) {
        this.unbreakable = unbreakable;
        return this;
    }

    public ConfigItemSlot displayName(String displayName) {
        this.displayName = displayName;
        return this;
    }

    public ConfigItemSlot lore(List<String> lore) {
        this.lore = lore;
        return this;
    }

    public ConfigItemSlot modelDataFloats(Float... floats) {
        this.modelComponents.setFloats(Arrays.asList(floats));
        return this;
    }

    public ConfigItemSlot modelDataStrings(String... strings) {
        this.modelComponents.setStrings(Arrays.asList(strings));
        return this;
    }

    public ConfigItemSlot modelDataFlags(Boolean... flags) {
        this.modelComponents.setFlags(Arrays.asList(flags));
        return this;
    }

    public List<Integer> getSlots() {
        return slots;
    }
}

