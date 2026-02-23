package fr.ibrakash.helper.configuration.objects.item;

import org.spongepowered.configurate.objectmapping.ConfigSerializable;

import java.util.Collections;
import java.util.List;

@ConfigSerializable
public  class ItemModelComponents {

    private List<Float> floats = Collections.emptyList();
    private List<String> strings = Collections.emptyList();
    private List<Boolean> flags = Collections.emptyList();

    public ItemModelComponents() {}

    public boolean isEmpty() {
        return floats.isEmpty() && strings.isEmpty() && flags.isEmpty();
    }

    public List<Float> getFloats() {
        return floats;
    }

    public void setFloats(List<Float> floats) {
        this.floats = floats;
    }

    public List<String> getStrings() {
        return strings;
    }

    public void setStrings(List<String> strings) {
        this.strings = strings;
    }

    public List<Boolean> getFlags() {
        return flags;
    }

    public void setFlags(List<Boolean> flags) {
        this.flags = flags;
    }
}