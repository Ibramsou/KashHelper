package fr.ibrakash.helper.configuration.readers;

import fr.ibrakash.helper.platform.KashAddon;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;

import java.util.HashMap;
import java.util.Map;

public abstract class DualConfigurationReader<K, V> extends ConfigurationReader {

    protected final Map<String, K> firstPathMap = new HashMap<>();
    protected final Map<String, V> secondPathMap = new HashMap<>();

    protected DualConfigurationReader(KashAddon<?> addon) {
        this(addon, null);
    }

    protected DualConfigurationReader(KashAddon<?> addon, String key) {
        super(addon, key);
        if (this.autoLoad()) {
            this.reload();
        }
    }

    @Override
    public void reload() {
        this.firstPathMap.clear();
        this.secondPathMap.clear();
        super.reload();
    }

    public K resolveKey(String path) {
        return this.resolveValue(this.firstPathMap, path, this::buildKey, this::fallbackKey);
    }

    public V resolveValue(String path) {
        return this.resolveValue(this.secondPathMap, path, this::buildValue, this::fallbackValue);
    }

    public K buildKey(ConfigurationNode node) throws SerializationException {
        throw new UnsupportedOperationException("Method implementation not defined");
    }

    public V buildValue(ConfigurationNode node) throws SerializationException {
        throw new UnsupportedOperationException("Method implementation not defined");
    }

    public K fallbackKey() {
        throw new UnsupportedOperationException("Method implementation not defined");
    }

    public V fallbackValue() {
        throw new UnsupportedOperationException("Method implementation not defined");
    }

    public Map<String, K> getFirstPathMap() {
        return firstPathMap;
    }

    public Map<String, V> getSecondPathMap() {
        return secondPathMap;
    }
}
