package fr.ibrakash.helper.configuration.objects.stream;

import org.spongepowered.configurate.objectmapping.ConfigSerializable;

import java.util.List;

@ConfigSerializable
public class ConfigFilterMode {

    private String text = "all";
    private List<String> streams = List.of();

    public ConfigFilterMode() {}

    public List<String> getStreams() {
        return streams;
    }

    public String getText() {
        return text;
    }
}
