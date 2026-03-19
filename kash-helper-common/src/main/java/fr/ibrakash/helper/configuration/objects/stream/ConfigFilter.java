package fr.ibrakash.helper.configuration.objects.stream;

import org.spongepowered.configurate.objectmapping.ConfigSerializable;

import java.util.*;

@ConfigSerializable
public class ConfigFilter {

    private String selectedEntry = "<green>- %mode%";
    private String unSelectedEntry = "<gray>- %mode%";
    private List<ConfigFilterMode> modes = new ArrayList<>();

    private transient String id;

    public ConfigFilter() {}

    public String getFilterId() {
        return id;
    }

    public String getSelectedEntry() {
        return selectedEntry;
    }

    public String getUnSelectedEntry() {
        return unSelectedEntry;
    }

    public List<ConfigFilterMode> getModes() {
        return modes;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        ConfigFilter that = (ConfigFilter) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
