package fr.ibrakash.helper.configuration.objects.stream;

import org.spongepowered.configurate.objectmapping.ConfigSerializable;

import java.util.*;
import java.util.function.Consumer;

@ConfigSerializable
public abstract class ConfigStream {

    protected Map<String, ConfigFilter> filters = new HashMap<>();

    public ConfigStream() {}

    public Map<String, ConfigFilter> getFilters() {
        return filters;
    }

    public void loadFilters(Consumer<ConfigFilter> consumer) {
        filters.forEach((s, configFilter) -> {
            configFilter.setId(s);
            consumer.accept(configFilter);
        });
    }
}
