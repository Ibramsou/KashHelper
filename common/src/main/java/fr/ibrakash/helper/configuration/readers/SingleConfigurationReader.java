package fr.ibrakash.helper.configuration.readers;

import fr.ibrakash.helper.platform.KashAddon;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;

import java.util.HashMap;
import java.util.Map;

public abstract class SingleConfigurationReader<V> extends ConfigurationReader {

    protected final Map<String, V> pathMap = new HashMap<>();

    protected SingleConfigurationReader(KashAddon<?> addon) {
        this(addon, null);
    }

    protected SingleConfigurationReader(KashAddon<?> addon, String key) {
        super(addon, key);
        if (this.autoLoad()) {
            this.reload();
        }
    }

    public V resolve(String path) {
        return this.resolveValue(this.pathMap, path, this::buildValue, this::fallBackValue);
    }

    public V build() throws SerializationException {
        throw new UnsupportedOperationException("Method implementation not defined");
    }

    public V fallBackValue() {
        throw new UnsupportedOperationException("Method implementation not defined");
    }

    @Override
    public void reload() {
        // Ensure cache stays in sync with underlying configuration.
        this.pathMap.clear();
        super.reload();
    }

    public Map<String, V> getPathMap() {
        return pathMap;
    }

    public abstract V buildValue(ConfigurationNode node) throws SerializationException;
}
