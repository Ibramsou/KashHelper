package fr.ibrakash.helper.configuration.objects.action;

import org.spongepowered.configurate.objectmapping.ConfigSerializable;

import java.util.ArrayList;
import java.util.List;

@ConfigSerializable
public class ConfigAction {

    private List<ConfigClick> clickTypes = List.of(ConfigClick.ALL);
    private List<String> execute = new ArrayList<>();

    public ConfigAction execute(List<String> actions) {
        this.execute =  actions;
        return this;
    }

    public List<ConfigClick> getClickTypes() {
        return clickTypes;
    }

    public List<String> getExecute() {
        return execute;
    }
}
