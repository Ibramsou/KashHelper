package fr.ibrakash.helper.configuration.objects.item;

import fr.ibrakash.helper.configuration.objects.AbstractConfigItem;
import org.bukkit.Material;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@ConfigSerializable
public class ConfigGuiItem extends AbstractConfigItem {

    private char ingredientId = 'A';
    private List<String> actions = new ArrayList<>();

    public ConfigGuiItem() {}

    public ConfigGuiItem actions(String... actions) {
        this.actions = List.of(actions);
        return this;
    }

    public ConfigGuiItem ingredientCharacter(char character) {
        this.ingredientId = character;
        return this;
    }

    public ConfigGuiItem material(Material material) {
        this.material = material.name();
        return this;
    }

    public ConfigGuiItem amount(int amount) {
        this.amount = amount;
        return this;
    }

    public ConfigGuiItem unbreakable(boolean unbreakable) {
        this.unbreakable = unbreakable;
        return this;
    }

    public ConfigGuiItem displayName(String displayName) {
        this.displayName = displayName;
        return this;
    }

    public ConfigGuiItem lore(List<String> lore) {
        this.lore = lore;
        return this;
    }

    public ConfigGuiItem modelDataFloats(Float... floats) {
        this.modelComponents.setFloats(Arrays.asList(floats));
        return this;
    }

    public ConfigGuiItem modelDataStrings(String... strings) {
        this.modelComponents.setStrings(Arrays.asList(strings));
        return this;
    }

    public ConfigGuiItem modelDataFlags(Boolean... flags) {
        this.modelComponents.setFlags(Arrays.asList(flags));
        return this;
    }

    public char getIngredientId() {
        return ingredientId;
    }

    public List<String> getActions() {
        return actions;
    }
}
