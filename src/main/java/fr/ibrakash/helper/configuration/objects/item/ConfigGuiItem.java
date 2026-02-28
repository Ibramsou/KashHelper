package fr.ibrakash.helper.configuration.objects.item;

import fr.ibrakash.helper.configuration.objects.AbstractConfigItem;
import fr.ibrakash.helper.configuration.objects.action.ConfigAction;
import fr.ibrakash.helper.configuration.objects.action.ConfigGroupAction;
import org.bukkit.Material;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

@ConfigSerializable
public class ConfigGuiItem extends AbstractConfigItem {

    private char shapeCharacter = 'A';
    private ConfigGroupAction actions = new ConfigGroupAction();

    private transient String id;

    public ConfigGuiItem() {}

    public ConfigGuiItem actions(String... actions) {
        this.actions.getActions().add(new ConfigAction().execute(List.of(actions)));
        return this;
    }

    public ConfigGuiItem actions(ConfigAction... actions) {
        this.actions.getActions().addAll(List.of(actions));
        return this;
    }

    public ConfigGuiItem ingredientCharacter(char character) {
        this.shapeCharacter = character;
        return this;
    }

    public ConfigGuiItem material(String material) {
        this.material = material;
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

    public char getShapeCharacter() {
        return shapeCharacter;
    }

    public ConfigGroupAction getActions() {
        return actions;
    }

    public void setShapeCharacter(char shapeCharacter) {
        this.shapeCharacter = shapeCharacter;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public ConfigGuiItem clone() {
        ConfigGuiItem guiItem = new ConfigGuiItem();
        super.copyValues(this, guiItem);
        guiItem.shapeCharacter = shapeCharacter;
        guiItem.actions = actions;
        return guiItem;
    }
}
